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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@code ConfigurationBlocksTreeprocessor}.
 *
 * @author Phillip Webb
 */
public class ConfigurationBlocksTreeprocessorTests {

	private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

	private final List<LogRecord> logRecords = new ArrayList<>();

	ConfigurationBlocksTreeprocessorTests() {
		this.asciidoctor.registerLogHandler(this.logRecords::add);
	}

	@Test
	void generateConfigDataCreeaseListings() {
		String source = "[source,yaml,configblocks]\n";
		source += "----\n";
		source += "example.property.alpha: a\n";
		source += "----\n";
		String output = convert(source);
		System.out.println(output);
		assertThat(output).contains("listingblock primary");
		assertThat(output).contains("listingblock secondary");
		assertThat(output).contains("example.property.alpha=a");
		assertThat(output).contains("example.property.alpha: a");
		assertThat(output).contains("class=\"highlight\"");
		assertThat(output).contains("class=\"language-yaml\"");
		assertThat(output).contains("class=\"language-properties\"");
	}

	private String convert(String source) {
		Options options = new Options();
		options.setSafe(SafeMode.SERVER);
		return this.asciidoctor.convert(source, options);
	}

}
