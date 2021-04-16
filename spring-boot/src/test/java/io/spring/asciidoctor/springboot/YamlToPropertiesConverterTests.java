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
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link YamlToPropertiesConverter}.
 *
 * @author Phillip Webb
 */
class YamlToPropertiesConverterTests {

	private final YamlToPropertiesConverter converter = new YamlToPropertiesConverter();

	@ParameterizedTest
	@MethodSource("yamlFiles")
	void convertPropertiesToYaml(File yamlFile) throws Exception {
		String name = yamlFile.getName().split("\\.")[0];
		File propertiesFile = new File(yamlFile.getParentFile(), name + ".properties");
		List<String> source = readLines(yamlFile);
		List<String> converted = this.converter.convertLines(source);
		List<String> expected = readLines(propertiesFile);
		assertThat(converted).as(name).isEqualTo(expected);
	}

	private List<String> readLines(File propertiesFile) throws IOException {
		return Files.lines(propertiesFile.toPath()).collect(Collectors.toList());
	}

	static File[] yamlFiles() {
		String packageName = YamlToPropertiesConverterTests.class.getPackage().getName();
		File packageFolder = new File("src/test/resources/" + packageName.replace('.', '/'));
		return packageFolder.listFiles((FileFilter) (pathname) -> isYamlFile(pathname));
	}

	static boolean isYamlFile(File file) {
		return file.getName().endsWith(".yaml");
	}

}
