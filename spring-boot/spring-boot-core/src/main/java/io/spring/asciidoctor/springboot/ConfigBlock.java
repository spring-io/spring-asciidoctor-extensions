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

import java.util.Arrays;
import java.util.List;

/**
 * A block of {@code .properties} or {@code .yaml} config data.
 *
 * @author Phillip Webb
 */
public class ConfigBlock {

	private final String title;

	private final String language;

	private final List<String> content;

	public ConfigBlock(String title, String language, String... content) {
		this(title, language, Arrays.asList(content));
	}

	public ConfigBlock(String title, String language, List<String> content) {
		this.title = title;
		this.language = language;
		this.content = content;
	}

	public String getTitle() {
		return this.title;
	}

	public String getLanguage() {
		return this.language;
	}

	public List<String> getContent() {
		return this.content;
	}

}
