/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.asciidoctor.javadoc;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.junit.Test;

/**
 * Tests for {@link JavadocInlineMacroProcessor}.
 *
 * @author Andy Wilkinson
 */
public class JavadocInlineMacroProcessorTests {

	@Test
	public void postProcessorIsApplied() {
		Options options = new Options();
		Attributes attributes = new Attributes();
		attributes.setAttribute("javadoc-file-root", "/Users/awilkinson/dev/spring/spring-restdocs/build/api");
		attributes.setAttribute("javadoc-http-root", "https://docs.spring.io/spring-restdocs/current/api");
		options.setAttributes(attributes);
		options.setHeaderFooter(true);
		String converted = Asciidoctor.Factory.create().convert(String.format("Please refer to the javadoc:RestDocumentationResultHandler#foo(String)[javadoc for `RestDocumentationResultHandler`] for further details"), options);
		System.out.println(converted);
	}

}
