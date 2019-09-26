package io.spring.asciidoctor.javadoc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class JavadocIdentifier {

	private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("(?:([^#]*)\\.)?([^#]+)(?:#([^\\(]+)(?:\\((.+)\\))?)?");

	private final Optional<String> packageName;

	private final String className;

	private final Optional<MethodIdentifier> method;

	JavadocIdentifier(String identifier) {
		Matcher matcher = IDENTIFIER_PATTERN.matcher(identifier);
		if (!matcher.matches()) {
			throw new IllegalArgumentException(identifier + " is not a valid javadoc identifier");
		}
		String packageName = matcher.group(1);
		this.packageName = packageName == null ? Optional.empty() : Optional.of(packageName);
		this.className = matcher.group(2);
		String methodName = matcher.group(3);
		if (methodName == null) {
			this.method = Optional.empty();
		}
		else {
			String parameters = matcher.group(4);
			if (parameters == null) {
				this.method = Optional.of(new MethodIdentifier(methodName));
			}
			else {
				this.method = Optional.of(new MethodIdentifier(methodName, Stream.of(parameters.split(",")).map(String::trim).collect(Collectors.toList())));
			}
		}
	}

	Optional<String> getPackageName() {
		return this.packageName;
	}

	String getClassName() {
		return this.className;
	}

	Optional<MethodIdentifier> getMethod() {
		return this.method;
	}

	static class MethodIdentifier {

		private final String name;

		private final List<String> parameters;

		MethodIdentifier(String name) {
			this(name, Collections.emptyList());
		}

		MethodIdentifier(String name, List<String> parameters) {
			if (parameters == null) {
				throw new IllegalArgumentException("parameters must not be null");
			}
			this.name = name;
			this.parameters = parameters;
		}

		String getName() {
			return name;
		}

		List<String> getParameters() {
			return parameters;
		}

	}

}