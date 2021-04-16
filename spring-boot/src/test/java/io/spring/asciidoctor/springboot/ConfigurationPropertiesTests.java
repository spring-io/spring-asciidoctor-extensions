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

package io.spring.asciidoctor.springboot;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConfigurationProperties}.
 *
 * @author Andy Wilkinson
 */
class ConfigurationPropertiesTests {

	@Test
	void loadMetadataFromSingleSource() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(new URLClassLoader(
				new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() }, null));
		assertThat(configurationProperties.size()).isEqualTo(3);
		assertThat(configurationProperties.find("project.a.alpha")).isNotNull();
		assertThat(configurationProperties.find("project.a.bravo-property")).isNotNull();
		assertThat(configurationProperties.find("project.a.charlie")).isNotNull();
	}

	@Test
	void loadMetadataFromMultipleSources() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL(),
						new File("src/test/resources/metadata/project-b").toURI().toURL() }, null));
		assertThat(configurationProperties.find("project.a.alpha")).isNotNull();
		assertThat(configurationProperties.find("project.a.bravo-property")).isNotNull();
		assertThat(configurationProperties.find("project.a.charlie")).isNotNull();
		assertThat(configurationProperties.size()).isEqualTo(6);
		assertThat(configurationProperties.find("project.b.alpha")).isNotNull();
		assertThat(configurationProperties.find("project.b.bravo-property")).isNotNull();
		assertThat(configurationProperties.find("project.b.charlie")).isNotNull();
	}

	@Test
	void whenPropertyIsNotDeprecatedInMetadataIsDeprecatedReturnsFalse() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() }));
		assertThat(configurationProperties.find("project.a.alpha").isDeprecated()).isFalse();
	}

	@Test
	void whenPropertyIsDeprecatedInMetadataIsDeprecatedReturnsTrue() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() }));
		assertThat(configurationProperties.find("project.a.bravo-property").isDeprecated()).isTrue();
	}

	@Test
	void whenPropertyIsAMapInMetadataIsMapReturnsTrue() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() }));
		assertThat(configurationProperties.find("project.a.charlie").isMap()).isTrue();
	}

	@Test
	void whenPropertyIsNotAMapInMetadataIsMapReturnsFalse() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() }));
		assertThat(configurationProperties.find("project.a.alpha").isMap()).isFalse();
	}

	@Test
	void whenPropertyNotInTheMetadataHasAMapAncestorItCanBeFound() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() }));
		assertThat(configurationProperties.find("project.a.charlie.beneath-map")).isNotNull();
	}

	@Test
	void whenPropertyNotInTheMetadataHasANonMapAncestorItCannotBeFound() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() }));
		assertThat(configurationProperties.find("project.a.alpha.beneath-non-map")).isNull();
	}

	@Test
	void whenPropertyInTheMetadataIsSearchedForWithArrayIndexSuffixItCanBeFound() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() }));
		ConfigurationProperty property = configurationProperties.find("project.a.alpha[2]");
		assertThat(property).isNotNull();
		assertThat(property.getName()).isEqualTo("project.a.alpha");
	}

	@Test
	void whenPropertyNotInTheMetadataIsSearchedForWithArrayIndexSuffixItCannotBeFound() throws MalformedURLException {
		ConfigurationProperties configurationProperties = ConfigurationProperties.fromClasspath(
				new URLClassLoader(new URL[] { new File("src/test/resources/metadata/project-a").toURI().toURL() }));
		assertThat(configurationProperties.find("project.a.delta[2]")).isNull();
	}

}
