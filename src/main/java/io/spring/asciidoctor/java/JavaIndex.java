package io.spring.asciidoctor.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.spring.asciidoctor.asm.AnnotationVisitor;
import io.spring.asciidoctor.asm.ClassReader;
import io.spring.asciidoctor.asm.ClassVisitor;
import io.spring.asciidoctor.asm.MethodVisitor;
import io.spring.asciidoctor.asm.Opcodes;
import io.spring.asciidoctor.asm.Type;

final class JavaIndex {

	private final Map<String, ClassDescriptor> fullyQualifiedClasses;

	private final Map<String, List<ClassDescriptor>> classes = new HashMap<>();

	JavaIndex(String basePackage) {
		String basePath = basePackage.replace('.', '/');
		Set<URL> urls = new HashSet<>();
		try {
			urls.addAll(Collections.list(getClass().getClassLoader().getResources(basePath)));
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		this.fullyQualifiedClasses = urls.stream()
				.flatMap((url) -> index(basePath, url))
				.collect(Collectors.toMap((descriptor) -> descriptor.getName(), Function.identity()));
		this.fullyQualifiedClasses.forEach((name, descriptor) -> {
			this.classes.computeIfAbsent(name.substring(name.lastIndexOf(".") + 1), (n) -> new ArrayList<>()).add(descriptor);
		});
	}

	int size() {
		return this.fullyQualifiedClasses.size();
	}

	private Stream<ClassDescriptor> index(String basePath, URL url) {
		try {
			if ("file".equals(url.getProtocol())) {
				File file = new File(url.toURI());
				if (file.isDirectory()) {
					PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.class");
					return Files.walk(file.toPath())
						.filter(matcher::matches)
						.map(Path::toFile)
						.map(this::index)
						.filter(Optional::isPresent)
						.map(Optional::get);
				}
			}
			else {
				URLConnection connection = url.openConnection();
				if (connection instanceof JarURLConnection) {
					JarFile jar = ((JarURLConnection)connection).getJarFile();
					return Collections.list(jar.entries()).stream()
						.filter((entry) -> !entry.isDirectory())
						.filter((entry) -> {
							return entry.getName().startsWith(basePath);
						})
						.filter((entry) -> entry.getName().endsWith(".class"))
						.map((entry) -> index(url, entry.getName()))
						.filter(Optional::isPresent)
						.map(Optional::get);
				}
			}
			return Stream.of();
		} catch (Exception ex) {
			return Stream.of();
		}
	}

	private Optional<ClassDescriptor> index(File file) {
		try {
			return index(new FileInputStream(file));
		}
		catch (IOException ex) {
			return Optional.empty();
		}
	}

	private Optional<ClassDescriptor> index(URL url, String entryName) {
		try (JarFile jar = ((JarURLConnection)url.openConnection()).getJarFile()) {
			return index(jar.getInputStream(jar.getJarEntry(entryName)));
		}
		catch (IOException ex) {
			return Optional.empty();
		}
	}

	private Optional<ClassDescriptor> index(InputStream byteCodeStream) {
		try {
			ClassReader reader = new ClassReader(byteCodeStream);
			MethodDescribingClassVisitor visitor = new MethodDescribingClassVisitor();
			reader.accept(visitor, 0);
			return Optional.of(new ClassDescriptor(reader.getClassName().replace('/', '.'), visitor.methods));
		} catch (IOException ex) {
			return Optional.ofNullable(null);
		}
	}

	ClassDescriptor get(String className) {
		ClassDescriptor descriptor = this.fullyQualifiedClasses.get(className);
		if (descriptor != null) {
			return descriptor;
		}
		List<ClassDescriptor> descriptors = this.classes.get(className);
		if (descriptors == null || descriptors.isEmpty()) {
			throw new IllegalStateException("Unknown class '" + className + "'");
		}
		if (descriptors.size() > 1) {
			StringBuilder message = new StringBuilder("Found multiple classes named '");
			message.append(className);
			message.append("':\n\n");
			descriptors.stream().map(ClassDescriptor::getName).sorted().forEach((match) -> {
				message.append("    ");
				message.append(match);
				message.append("\n");
			});
			message.append("\n");
			message.append("Use a fully-qualified name to identifiy a specific class");
			throw new IllegalStateException(message.toString());
		}
			return descriptors.get(0);
	}

	static final class ClassDescriptor {

		private final String name;

		private final Set<MethodDescriptor> methods;

		private ClassDescriptor(String name, Set<MethodDescriptor> methods) {
			this.name = name;
			this.methods = methods;
		}

		String getName() {
			return this.name;
		}

		Set<MethodDescriptor> getMethods() {
			return this.methods;
		}

		MethodDescriptor getMethod(String methodName) {
			List<MethodDescriptor> matches = this.methods.stream()
					.filter((method) -> methodName.equals(method.name))
					.collect(Collectors.toList());
			if (matches.size() == 1) {
				return matches.get(0);
			}
			if (matches.isEmpty()) {
				throw new IllegalStateException("No method named '" + methodName + "' found on class '" + this.name + "'");
			}
			StringBuilder message = new StringBuilder("Class '");
			message.append(this.name);
			message.append("' contains multiple methods named '");
			message.append(methodName);
			message.append("':\n\n");
			matches.stream().map((match) -> "    " + asString(match) + "\n").sorted().forEach(message::append);
			message.append("\n");
			message.append("Use args to identity a specific method");
			throw new IllegalStateException(message.toString());
		}

		private String asString(MethodDescriptor match) {
			StringBuilder builder = new StringBuilder(match.getName());
			builder.append("(");
			builder.append(String.join(",", match.getArgumentTypes()));
			builder.append(")");
			return builder.toString();
		}

		MethodDescriptor getMethod(String methodName, List<String> arguments) {
			List<MethodDescriptor> matches = this.methods.stream()
					.filter((method) -> methodName.equals(method.name))
					.filter((method) -> method.argumentTypes.equals(arguments))
					.collect(Collectors.toList());
			if (matches.size() == 1) {
				return matches.get(0);
			}
			if (matches.isEmpty()) {
				throw new IllegalStateException("No method named '" + methodName + "' with arguments " + arguments + " found on class '" + this.name);
			}
			throw new IllegalStateException("Class '" + this.name + "' contains multiple methods named '" + methodName + "' with arguments " + arguments);
		}

	}

	static final class MethodDescriptor {

		private final String name;

		private final List<String> argumentTypes;

		private final boolean deprecated;

		private MethodDescriptor(String name, List<String> argumentTypes, boolean deprecated) {
			this.name = name;
			this.argumentTypes = argumentTypes;
			this.deprecated = deprecated;
		}

		String getName() {
			return name;
		}

		List<String> getArgumentTypes() {
			return argumentTypes;
		}

		boolean isDeprecated() {
			return this.deprecated;
		}

	}

	private static final class MethodDescribingClassVisitor extends ClassVisitor {

		private final Set<MethodDescriptor> methods = new HashSet<>();

		private MethodDescribingClassVisitor() {
			super(Opcodes.ASM6);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			return new MethodVisitor(Opcodes.ASM6) {

				private boolean deprecated;

				@Override
				public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
					if (Type.getType(Deprecated.class).equals(Type.getType(desc))) {
						this.deprecated = true;
					}
					return null;
				}

				@Override
				public void visitEnd() {
					methods.add(new MethodDescriptor(
							name,
							Stream.of(Type.getArgumentTypes(desc))
									.map(Type::getClassName)
									.collect(Collectors.toList()),
							this.deprecated));
				}

			};
		}

	}

}
