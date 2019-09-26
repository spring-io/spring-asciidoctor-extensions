package io.spring.asciidoctor.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.spring.asciidoctor.java.JavaIndex.ClassDescriptor;

public class JavaIndexTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void findsAllClasses() throws IOException {
		JavaIndex index = new JavaIndex("com.example");
		assertThat(index.size()).isEqualTo(4);
		index.get("com.example.one.Alpha");
		index.get("com.example.one.Bravo");
		index.get("com.example.two.Bravo");
		index.get("com.example.two.Charlie");
	}

	@Test
	public void findsAllClassesInSubpackage() throws IOException {
		JavaIndex index = new JavaIndex("com.example.one");
		assertThat(index.size()).isEqualTo(2);
		index.get("com.example.one.Alpha");
		index.get("com.example.one.Bravo");
	}

	@Test
	public void classCanBeRetrievedBySimpleName() throws IOException {
		JavaIndex index = new JavaIndex("com.example.one");
		index.get("Alpha");
	}

	@Test
	public void getFailsWhenMultipleClassesHaveSameSimpleName() throws IOException {
		JavaIndex index = new JavaIndex("com.example");
		this.thrown.expectMessage(equalTo("Found multiple classes named 'Bravo':\n\n"
				+ "    com.example.one.Bravo\n    com.example.two.Bravo\n\n"
				+ "Use a fully-qualified name to identify a specific class"));
		index.get("Bravo");
	}

	@Test
	public void findsAllMethodsOnClass() throws IOException {
		JavaIndex index = new JavaIndex("com.example");
		ClassDescriptor alpha = index.get("com.example.one.Alpha");
		assertThat(alpha.getMethods()).hasSize(14);
		alpha.getMethod("<init>");
		alpha.getMethod("privateMethod");
		alpha.getMethod("packagePrivateMethod");
		alpha.getMethod("protectedMethod");
		alpha.getMethod("publicMethod");
		alpha.getMethod("withArg");
		alpha.getMethod("withMultipleArgs");
		alpha.getMethod("withArrayArg");
		alpha.getMethod("withVarargs");
		alpha.getMethod("overloaded", Collections.emptyList());
		alpha.getMethod("overloaded", Arrays.asList("java.lang.Object"));
		alpha.getMethod("overloaded", Arrays.asList("java.lang.String"));
		alpha.getMethod("overloaded", Arrays.asList("java.lang.Object", "java.lang.Object"));
		alpha.getMethod("deprecated");
	}

	@Test
	public void getMethodFailsWhenMultipleMethodsHaveSameSimpleName() {
		JavaIndex index = new JavaIndex("com.example");
		ClassDescriptor alpha = index.get("com.example.one.Alpha");
		this.thrown.expectMessage(equalTo("Class 'com.example.one.Alpha' contains multiple methods named 'overloaded':\n\n" +
				"    overloaded()\n" +
				"    overloaded(java.lang.Object)\n" +
				"    overloaded(java.lang.Object,java.lang.Object)\n" +
				"    overloaded(java.lang.String)\n" +
				"\n" +
				"Use args to identity a specific method"));
		alpha.getMethod("overloaded");
	}

}
