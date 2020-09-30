/*
 * Copyright 2020 the original author or authors.
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

import java.util.List;
import java.util.function.Consumer;

/**
 * Generates {@link ConfigBlock} in different formats. Currently supports {@code .yaml} to
 * {@code .properties}.
 *
 * @author Phillip Webb
 */
public class ConfigBlocksGenerator {

	private final Logger logger;

	private final YamlToPropertiesConverter yamlToProperties = new YamlToPropertiesConverter();

	public ConfigBlocksGenerator(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Apply generation based on the given input.
	 * @param in the input
	 * @param out a customer that should be called with each output
	 */
	public void apply(ConfigBlock in, Consumer<ConfigBlock> out) {
		this.logger.debug("Generating condig data blocks");
		if (!"yaml".equalsIgnoreCase(in.getLanguage())) {
			throw new IllegalArgumentException("Block generation is only supported for YAML inputs");
		}
		out.accept(createConfigData(in, "properties", this.yamlToProperties.convertLines(in.getContent())));
		out.accept(createConfigData(in, "yaml", in.getContent()));
	}

	private ConfigBlock createConfigData(ConfigBlock in, String language, List<String> content) {
		return new ConfigBlock(getTitle(in, language), language, content);
	}

	private String getTitle(ConfigBlock in, String language) {
		String title = in.getTitle();
		String langaugeName = Character.toUpperCase(language.charAt(0)) + language.substring(1).toLowerCase();
		if (title == null || title.isEmpty()) {
			return langaugeName;
		}
		if (title.endsWith("." + in.getLanguage().toLowerCase())) {
			return title.substring(0, title.length() - in.getLanguage().length()) + language.toLowerCase();
		}
		return title + " (" + langaugeName + ")";
	}

}
