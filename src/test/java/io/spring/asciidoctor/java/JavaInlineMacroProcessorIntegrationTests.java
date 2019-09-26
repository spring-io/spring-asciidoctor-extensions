package io.spring.asciidoctor.java;

import static org.assertj.core.api.Assertions.assertThat;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JavaInlineMacroProcessorIntegrationTests {

	private static final String JAVADOC_URL= "https://docs.example.com/current/api/";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void classTargetProducesLinkToClassJavadocWhenNameIsUnique() {
		String converted = Asciidoctor.Factory.create().convert(String.format("java:class[name=\"Alpha\"]"), createOptions());
		assertThat(converted).contains("<a href=\"" + JAVADOC_URL + "com/example/one/Alpha.html\">`Alpha`</a>");
	}

	@Test
	public void conversionFailsWhenClassTargetHasNameThatIsAmbiguous() {
		this.thrown.expect(IllegalStateException.class);
		Asciidoctor.Factory.create().convert(String.format("java:class[name=\"Bravo\"]"), createOptions());
	}

	@Test
	public void methodTargetProducesLinkToMethodJavadocWhenNameIsUnique() {
		String converted = Asciidoctor.Factory.create().convert(String.format("java:method[class=\"Alpha\",name=\"withArg\"]"), createOptions());
		assertThat(converted).contains("<a href=\"" + JAVADOC_URL + "com/example/one/Alpha.html#withArg-java.lang.String-\">`withArg`</a>");
	}

	@Test
	public void conversionFailsWhenMethodTargetHasNameThatIsAmbiguous() {
		this.thrown.expect(IllegalStateException.class);
		Asciidoctor.Factory.create().convert(String.format("java:method[class=\"Alpha\",name=\"overloaded\"]"), createOptions());
	}

	@Test
	public void postProcessorIsApplied() {
		Options options = createOptions();
		String converted = Asciidoctor.Factory.create().convert(String.format("java:class[name=\"org.springframework.boot.SpringApplication\"]"), options);
		System.out.println(converted);
		converted = Asciidoctor.Factory.create().convert("java:method[name=\"run\",class=\"org.springframework.boot.SpringApplication\",args=\"java.lang.Class[\\],java.lang.String[\\]\"]", options);
		System.out.println(converted);
		converted = Asciidoctor.Factory.create().convert("java:method[name=\"run\",class=\"org.springframework.boot.SpringApplication\",args=\"java.lang.Class,java.lang.String...\"]", options);
		System.out.println(converted);
		converted = Asciidoctor.Factory.create().convert("java:method[name=\"exit\",class=\"org.springframework.boot.SpringApplication\",varargs]", options);
		System.out.println(converted);
		converted = Asciidoctor.Factory.create().convert("java:method[name=\"exit\",class=\"SpringApplication\",varargs]", options);
		System.out.println(converted);
		converted = Asciidoctor.Factory.create().convert("java:method[name=\"setWebEnvironment\",class=\"org.springframework.boot.SpringApplication\",deprecated]", options);
		System.out.println(converted);
	}

	private Options createOptions() {
		Options options = new Options();
		Attributes attributes = new Attributes();
		attributes.setAttribute("java-base-package", "com.example");
		attributes.setAttribute("javadoc-url", JAVADOC_URL);
		options.setAttributes(attributes);
		options.setHeaderFooter(true);
		return options;
	}

}
