package io.spring.asciidoctor.javadoc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JavadocIdentifierTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void unqualifiedClass() {
		JavadocIdentifier identifier = new JavadocIdentifier("FooBarBaz");
		assertThat(identifier.getPackageName().isPresent()).isFalse();
		assertThat(identifier.getClassName()).isEqualTo("FooBarBaz");
	}

	@Test
	public void unqualifiedClassWithTrailingHashIsRejected() {
		this.thrown.expect(IllegalArgumentException.class);
		new JavadocIdentifier("FooBarBaz#");
	}

	@Test
	public void qualifiedClass() {
		JavadocIdentifier identifier = new JavadocIdentifier("com.example.FooBarBaz");
		assertThat(identifier.getPackageName()).isEqualTo("com.example");
		assertThat(identifier.getClassName()).isEqualTo("FooBarBaz");
	}

	@Test
	public void qualifiedClassWithTrailingHashIsRejected() {
		this.thrown.expect(IllegalArgumentException.class);
		new JavadocIdentifier("com.example.FooBarBaz#");
	}

	@Test
	public void methodOnUnqualifiedClass() {
		JavadocIdentifier identifier = new JavadocIdentifier("FooBarBaz#someMethod");
		assertThat(identifier.getPackageName()).isNull();
		assertThat(identifier.getClassName()).isEqualTo("FooBarBaz");
		assertThat(identifier.getMethod().isPresent()).isTrue() ;
		assertThat(identifier.getMethod().get().getName()).isEqualTo("someMethod");
	}

	@Test
	public void methodOnQualifiedClass() {
		JavadocIdentifier identifier = new JavadocIdentifier("com.example.FooBarBaz#someMethod");
		assertThat(identifier.getPackageName()).isEqualTo("com.example");
		assertThat(identifier.getClassName()).isEqualTo("FooBarBaz");
		assertThat(identifier.getMethod().isPresent()).isTrue() ;
		assertThat(identifier.getMethod().get()).isEqualTo("someMethod");
	}

	@Test
	public void methodWithParenthesesButNoParametersIsRejected() {
		this.thrown.expect(IllegalArgumentException.class);
		new JavadocIdentifier("com.example.FooBarBaz#someMethod()");
	}

	@Test
	public void methodWithOneParameter() {
		JavadocIdentifier identifier = new JavadocIdentifier("com.example.FooBarBaz#someMethod(String)");
		assertThat(identifier.getPackageName()).isEqualTo("com.example");
		assertThat(identifier.getClassName()).isEqualTo("FooBarBaz");
		assertThat(identifier.getMethod().isPresent()).isTrue();
		assertThat(identifier.getMethod().get().getName()).isEqualTo("someMethod");
		assertThat(identifier.getMethod().get().getParameters()).containsExactly("String");
	}

	@Test
	public void varargsMethodWithOneParameter() {
		JavadocIdentifier identifier = new JavadocIdentifier("com.example.FooBarBaz#someMethod(String...)");
		assertThat(identifier.getPackageName()).isEqualTo("com.example");
		assertThat(identifier.getClassName()).isEqualTo("FooBarBaz");
		assertThat(identifier.getMethod().isPresent()).isTrue();
		assertThat(identifier.getMethod().get().getName()).isEqualTo("someMethod");
		assertThat(identifier.getMethod().get().getParameters()).containsExactly("String...");
	}

	@Test
	public void methodWithMultipleParameters() {
		JavadocIdentifier identifier = new JavadocIdentifier("com.example.FooBarBaz#someMethod(String, Object)");
		assertThat(identifier.getPackageName().get()).isEqualTo("com.example");
		assertThat(identifier.getClassName()).isEqualTo("FooBarBaz");
		assertThat(identifier.getMethod().isPresent()).isTrue();
		assertThat(identifier.getMethod().get().getName()).isEqualTo("someMethod");
		assertThat(identifier.getMethod().get().getParameters()).containsExactly("String", "Object");
	}

	@Test
	public void varargsMethodWithMultipleParameters() {
		JavadocIdentifier identifier = new JavadocIdentifier("com.example.FooBarBaz#someMethod(String, Object...)");
		assertThat(identifier.getPackageName()).isEqualTo("com.example");
		assertThat(identifier.getClassName()).isEqualTo("FooBarBaz");
		assertThat(identifier.getMethod().isPresent()).isTrue();
		assertThat(identifier.getMethod().get().getName()).isEqualTo("someMethod");
		assertThat(identifier.getMethod().get().getParameters()).containsExactly("String", "Object...");
	}

}
