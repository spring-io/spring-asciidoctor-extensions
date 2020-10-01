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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Tests for {@link ConfigBlocksGenerator}.
 *
 * @author Phillip Webb
 */
class ConfigBlocksGeneratorTests {

	private final ConfigBlocksGenerator generator = new ConfigBlocksGenerator();

	private final List<ConfigBlock> out = new ArrayList<>();

	@Test
	void applyWhenInputIsNotYamlThrowsException() {
		ConfigBlock in = new ConfigBlock(null, "properties", "foo=bar");
		assertThatIllegalArgumentException().isThrownBy(() -> this.generator.apply(in, this.out::add))
				.withMessage("Block generation is only supported for YAML inputs");
	}

	@Test
	void applyWhenInputIsYamlProducesPropertiesAndYaml() {
		ConfigBlock in = new ConfigBlock(null, "yaml", "foo: bar");
		this.generator.apply(in, this.out::add);
		assertThat(this.out.get(0).getContent()).containsExactly("foo=bar");
		assertThat(this.out.get(0).getLanguage()).isEqualTo("properties");
		assertThat(this.out.get(1).getContent()).containsExactly("foo: bar");
		assertThat(this.out.get(1).getLanguage()).isEqualTo("yaml");
	}

	@Test
	void applyWhenHasNoTitleProducesTitles() {
		ConfigBlock in = new ConfigBlock(null, "yaml", "foo: bar");
		this.generator.apply(in, this.out::add);
		assertThat(this.out.get(0).getTitle()).isEqualTo("Properties");
		assertThat(this.out.get(1).getTitle()).isEqualTo("Yaml");
	}

	@Test
	void applyWhenHasFilenameTitleProducesModifiedTitles() {
		ConfigBlock in = new ConfigBlock("application.yaml", "yaml", "foo: bar");
		this.generator.apply(in, this.out::add);
		assertThat(this.out.get(0).getTitle()).isEqualTo("application.properties");
		assertThat(this.out.get(1).getTitle()).isEqualTo("application.yaml");
	}

	@Test
	void applyWhenHasTextTitleProducesModifiedTitles() {
		ConfigBlock in = new ConfigBlock("Example", "yaml", "foo: bar");
		this.generator.apply(in, this.out::add);
		assertThat(this.out.get(0).getTitle()).isEqualTo("Example (Properties)");
		assertThat(this.out.get(1).getTitle()).isEqualTo("Example (Yaml)");
	}

}
