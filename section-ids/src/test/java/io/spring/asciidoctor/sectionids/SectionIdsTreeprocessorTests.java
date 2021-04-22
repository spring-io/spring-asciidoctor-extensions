/*
 * Copyright 2014-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.asciidoctor.sectionids;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SectionIdsTreeprocessor}.
 *
 * @author Andy Wilkinson
 */
class SectionIdsTreeprocessorTests {

	private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

	private final List<LogRecord> logRecords = new ArrayList<>();

	SectionIdsTreeprocessorTests() {
		this.asciidoctor.registerLogHandler(this.logRecords::add);
	}

	@Test
	void topLevelIdsMustBeKebabCase() throws FileNotFoundException, IOException {
		File adocFile = new File(
				"src/test/resources/io/spring/asciidoctor/sectionids/SectionIdsTreeprocessorTests_topLevelIdsMustBeKebabCase.adoc");
		convert(new String(Files.readAllBytes(adocFile.toPath()), StandardCharsets.UTF_8));
		assertThat(this.logRecords).hasSize(3);
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsOnly(Severity.WARN);
		assertThat(this.logRecords).extracting(LogRecord::getMessage).containsExactly(
				"Top-level section ID 'snake_case' should use kebab-case",
				"Top-level section ID 'trailing-dash-kebab-case-' should use kebab-case",
				"Top-level section ID 'double--dash-kebab-case' should use kebab-case");
	}

	@Test
	void nestedIdsMustBeParentIdDotKebabCase() throws FileNotFoundException, IOException {
		File adocFile = new File(
				"src/test/resources/io/spring/asciidoctor/sectionids/SectionIdsTreeprocessorTests_nestedIdsMustBeParentIdDotKebabCase.adoc");
		convert(new String(Files.readAllBytes(adocFile.toPath()), StandardCharsets.UTF_8));
		assertThat(this.logRecords).hasSize(4);
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsOnly(Severity.WARN);
		assertThat(this.logRecords).extracting(LogRecord::getMessage).containsExactly(
				"Section ID 'parent-id.child-one-grandchild-two' should start with 'parent-id.child-one.'",
				"'grandchild_three' tail of section ID 'parent-id.child-one.grandchild_three' should use kebab-case",
				"Section ID 'parent-id-child-two' should start with 'parent-id.'",
				"'child_three' tail of section ID 'parent-id.child_three' should use kebab-case");
	}

	private String convert(String source) {
		Options options = new Options();
		options.setSafe(SafeMode.SERVER);
		return this.asciidoctor.convert(source, options);
	}

}
