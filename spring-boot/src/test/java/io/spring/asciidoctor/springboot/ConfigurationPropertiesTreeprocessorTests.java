/*
 * Copyright 2014-2020 the original author or authors.
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

package io.spring.asciidoctor.springboot;

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
 * Tests for {@code ConfigurationPropertiesTreeprocessor}.
 *
 * @author Andy Wilkinson
 */
class ConfigurationPropertiesTreeprocessorTests {

	private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

	private final List<LogRecord> logRecords = new ArrayList<>();

	ConfigurationPropertiesTreeprocessorTests() {
		this.asciidoctor.registerLogHandler(this.logRecords::add);
	}

	@Test
	void whenPropertiesThatExistAreReferencedOnlyDebugRecordsAreLogged() {
		convert("[source,properties,configprops]\n" + //
				"----\n" + //
				"example.property.alpha=a\n" + //
				"example.property.bravo=b\n" + //
				"example.property.charlie=c\n" + //
				"----");
		assertThat(this.logRecords).hasSize(3);
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsOnly(Severity.DEBUG);
	}

	@Test
	void whenAPropertyThatDoesNotExistIsReferencedAWarnRecordIsLogged() {
		convert("[source,properties,configprops]\n" + //
				"----\n" + //
				"example.property.alpha=a\n" + //
				"does.not.exist=b\n" + //
				"example.property.charlie=c\n" + //
				"----");
		assertThat(this.logRecords).hasSize(3);
		assertThat(this.logRecords).extracting(LogRecord::getSeverity).containsExactlyInAnyOrder(Severity.DEBUG,
				Severity.DEBUG, Severity.WARN);
	}

	@Test
	void aListingWithoutConfigpropsIsNotValidated() {
		convert("[source,properties]\n" + //
				"----\n" + //
				"example.property.alpha=a\n" + //
				"does.not.exist=b\n" + //
				"example.property.charlie=c\n" + //
				"----");
		assertThat(this.logRecords).isEmpty();
	}

	@Test
	void whenSourceContainsADescriptionListTreeprocessingDoesNotFail() {
		convert("CPU:: The brain of the computer.\n" + //
				"Hard drive:: Permanent storage for operating system and/or user files.\n" + //
				"RAM:: Temporarily stores information the CPU uses during operation.\n" + //
				"Keyboard:: Used to enter text or control items on the screen.\n" + //
				"Mouse:: Used to point to and select items on your computer screen.\n" + //
				"Monitor:: Displays information in visual form using text and graphics.\n");
	}

	private String convert(String source) {
		Options options = new Options();
		options.setSafe(SafeMode.SERVER);
		return this.asciidoctor.convert(source, options);
	}

}
