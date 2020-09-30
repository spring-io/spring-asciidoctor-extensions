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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Converter that turns {@code .yaml} config data into {@code .properties}.
 *
 * @author Phillip Webb
 */
public class YamlToPropertiesConverter {

	private static final Pattern VALID_COMPONENT_NAME = Pattern.compile("^[a-z0-9][a-z0-9-\\.]*$");

	List<String> convert(List<String> source) {
		String content = getContent(source);
		List<Document> documents = new ArrayList<>();
		for (Object loaded : getYaml().loadAll(content)) {
			documents.add(new Document(asMap(loaded)));
		}
		List<String> result = new ArrayList<>();
		for (int i = 0; i < documents.size(); i++) {
			result.addAll(documents.get(i).getLines());
			if (i < documents.size() - 1) {
				result.add("#---");
			}
		}
		return result;
	}

	private String getContent(List<String> source) {
		StringBuilder builder = new StringBuilder();
		source.forEach((line) -> builder.append(line).append("\n"));
		return builder.toString();
	}

	private Yaml getYaml() {
		LoaderOptions loaderOptions = new LoaderOptions();
		loaderOptions.setAllowDuplicateKeys(false);
		Yaml yaml = new Yaml(loaderOptions);
		return yaml;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> asMap(Object object) {
		Map<String, Object> result = new LinkedHashMap<>();
		Map<Object, Object> map = (Map<Object, Object>) object;
		map.forEach((key, value) -> {
			String name = (key instanceof CharSequence) ? key.toString() : "[" + key + "]";
			value = (value instanceof Map) ? asMap(value) : value;
			result.put(name, value);
		});
		return result;
	}

	private static class Document extends Properties {

		private final Map<Object, Object> entries = new LinkedHashMap<>();

		Document(Map<String, Object> map) {
			flatten("", map);
		}

		@SuppressWarnings("unchecked")
		private void flatten(String path, Map<String, Object> source) {
			source.forEach((key, value) -> {
				String name = getName(path, key);
				if (value instanceof String) {
					put(name, value);
				}
				else if (value instanceof Map) {
					flatten(name, (Map<String, Object>) value);
				}
				else if (value instanceof Collection) {
					int index = 0;
					for (Object element : (Collection<Object>) value) {
						flatten(name, Collections.singletonMap("[" + (index++) + "]", element));
					}
				}
				else {
					put(name, (value != null) ? value : "");
				}
			});
		}

		private String getName(String path, String key) {
			String name = (key.startsWith("[") || VALID_COMPONENT_NAME.matcher(key).matches()) ? key : "[" + key + "]";
			if (path == null || "".equals(path)) {
				return name;
			}
			String separator = (!name.startsWith("[")) ? "." : "";
			return path + separator + name;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public synchronized Enumeration<Object> keys() {
			return Collections.enumeration((Set) this.entries.keySet());
		}

		@Override
		public synchronized Object put(Object key, Object value) {
			return this.entries.put(key, value);
		}

		@Override
		public synchronized Object get(Object key) {
			Object value = this.entries.get(key);
			return (value != null) ? String.valueOf(value) : null;
		}

		private List<String> getLines() {
			try {
				StringWriter writer = new StringWriter();
				store(writer, null);
				List<String> lines = new ArrayList<>(Arrays.asList(writer.toString().split("\n")));
				lines.remove(0);
				return lines;
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

	}

}
