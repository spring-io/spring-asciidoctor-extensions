/*
 * Copyright 2014-2023 the original author or authors.
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import io.spring.asciidoctor.springboot.ValidationSettings.Format;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationPropertyValidator}.
 *
 * @author Andy Wilkinson
 */
class ConfigurationPropertyValidatorTests {

	private final TestLogger logger = new TestLogger();

	private final ConfigurationPropertyValidator validator;

	ConfigurationPropertyValidatorTests() throws MalformedURLException {
		this.validator = new ConfigurationPropertyValidator(this.logger, ConfigurationProperties.fromClasspath(
				new TestLogger(),
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() })));
	}

	@Test
	void whenPropertyCanBeFoundASingleDebugMessageIsLogged() {
		assertThat(this.validator.validateProperty("project.a.alpha", ValidationSettings.DEFAULT))
			.isEqualTo("project.a.alpha");
		assertThat(this.logger).warnMessages().isEmpty();
		assertThat(this.logger).debugMessages()
			.containsExactly("Configuration property 'project.a.alpha' successfully validated.");
	}

	@Test
	void whenPropertyCannotBeFoundASingleWarnMessageIsLogged() {
		assertThat(this.validator.validateProperty("project.a.delta", ValidationSettings.DEFAULT))
			.isEqualTo("project.a.delta");
		assertThat(this.logger).warnMessages().containsExactly("Configuration property 'project.a.delta' not found.");
		assertThat(this.logger).debugMessages().isEmpty();
	}

	@Test
	void whenFormatIsEnvironmentVariableValidationResultIsFormattedCorrectly() {
		String result = this.validator.validateProperty("project.a.alpha",
				new ValidationSettings(false, Format.ENVIRONMENT_VARIABLE));
		assertThat(result).isEqualTo("PROJECT_A_ALPHA");
	}

	@Test
	void whenFormatIsEnvironmentVariableAndPropertyNameContainsDashesValidationResultIsFormattedCorrectly() {
		String result = this.validator.validateProperty("project.a.bravo-property",
				new ValidationSettings(false, Format.ENVIRONMENT_VARIABLE));
		assertThat(result).isEqualTo("PROJECT_A_BRAVOPROPERTY");
	}

	@Test
	void whenAnUndeprecatedPropertyIsExpectedToBeDeprecatedAWarnMessageIsLogged() {
		assertThat(this.validator.validateProperty("project.a.alpha", new ValidationSettings(true, Format.CANONICAL)))
			.isEqualTo("project.a.alpha");
		assertThat(this.logger).warnMessages()
			.containsExactly("Configuration property 'project.a.alpha' is not deprecated.");
		assertThat(this.logger).debugMessages().isEmpty();
	}

	@Test
	void whenADeprecatedPropertyIsNotExpectedToBeDeprecatedAWarnMessageIsLogged() {
		assertThat(this.validator.validateProperty("project.a.bravo-property",
				new ValidationSettings(false, Format.CANONICAL)))
			.isEqualTo("project.a.bravo-property");
		assertThat(this.logger).warnMessages()
			.containsExactly("Configuration property 'project.a.bravo-property' is deprecated.");
		assertThat(this.logger).debugMessages().isEmpty();
	}

	@Test
	void whenPropertyNotInTheMetadataHasAMapAncestorValidatePropertyReturnsAndLogsTheFullPropertyName() {
		assertThat(this.validator.validateProperty("project.a.charlie.beneath-map",
				new ValidationSettings(false, Format.CANONICAL)))
			.isEqualTo("project.a.charlie.beneath-map");
		assertThat(this.logger).warnMessages().isEmpty();
		assertThat(this.logger).debugMessages()
			.containsExactly("Configuration property 'project.a.charlie.beneath-map' successfully validated.");
	}

}
