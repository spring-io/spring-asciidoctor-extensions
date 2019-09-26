package io.spring.asciidoctor.javadoc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.InlineMacroProcessor;

public class JavadocInlineMacroProcessor extends InlineMacroProcessor {

	public JavadocInlineMacroProcessor() {
		super("javadoc");
	}

	@Override
	protected Object process(AbstractBlock parent, String identifier, Map<String, Object> config) {
		JavadocIdentifier javadocIdentifier = new JavadocIdentifier(identifier);
		File javadocRoot = new File((String)parent.getDocument().getAttr("javadoc-file-root"));
		List<File> matches = search(javadocRoot, file -> file.getName().equals(javadocIdentifier.getClassName() + ".html"));
		if (matches.isEmpty()) {
			throw new IllegalStateException("Could not find javadoc for " + javadocIdentifier);
		}
		if (matches.size() > 1) {
			throw new IllegalStateException("Found multiplpe matches for " + javadocIdentifier);
		}
		return parent.getDocument().getAttr("javadoc-http-root") + matches.get(0).getPath().substring(javadocRoot.getPath().length()) + "[" + config.get("text") + "]";
	}

	List<File> search(File dir, Predicate<File> filter) {
		List<File> matches = new ArrayList<File>();
		for (File candidate: dir.listFiles()) {
			if (candidate.isFile() && filter.test(candidate)) {
				matches.add(candidate);
			}
			else if (candidate.isDirectory()) {
				matches.addAll(search(candidate, filter));
			}
		}
		return matches;
	}

}
