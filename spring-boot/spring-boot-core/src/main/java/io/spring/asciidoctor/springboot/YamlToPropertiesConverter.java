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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
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
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Converter that turns {@code .yaml} config data into {@code .properties}.
 *
 * @author Phillip Webb
 */
public class YamlToPropertiesConverter {

	private static final Pattern VALID_COMPONENT_NAME = Pattern.compile("^[a-z0-9][a-z0-9-\\.]*$");

	private static final String PREFIX = YamlToPropertiesConverter.class.getName().replace(".", "").toLowerCase();

	private static final String BLANK_LINE = PREFIX + "blankline";

	private static final String COMMENT = PREFIX + "comment";

	private int counter;

	List<String> convertLines(List<String> source) {
		String content = getContent(source);
		try {
			List<Document> documents = convertContent(content);
			List<String> result = new ArrayList<>();
			for (int i = 0; i < documents.size(); i++) {
				result.addAll(documents.get(i).getLines());
				if (i < documents.size() - 1) {
					result.add("#---");
				}
			}
			return result;
		}
		catch (RuntimeException ex) {
			throw new IllegalStateException(ex.getMessage() + "\n" + content, ex);
		}
	}

	List<Document> convertContent(String content) {
		List<Document> documents = new ArrayList<>();
		for (Object loaded : getYaml().loadAll(content)) {
			documents.add(new Document(asMap(loaded)));
		}
		return documents;
	}

	private String getContent(List<String> source) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < source.size(); i++) {
			String line = source.get(i);
			String previousLine = (i > 0) ? source.get(i - 1) : "";
			builder.append(preProcess(line, previousLine)).append("\n");
		}
		return builder.toString();
	}

	private String preProcess(String line, String previousLine) {
		String indentation = getIndentation(previousLine);
		if (line.isEmpty()) {
			int i = this.counter++;
			return indentation + BLANK_LINE + i + ": " + i;
		}
		if (line.startsWith("# ")) {
			return indentation + COMMENT + (this.counter++) + ": \"" + line + "\"";
		}
		return line;
	}

	private String getIndentation(String previousLine) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < previousLine.length(); i++) {
			if (previousLine.charAt(i) != ' ') {
				return result.toString();
			}
			result.append(" ");
		}
		return result.toString();
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

	static class Document extends Properties {

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

		@Override
		public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
			this.entries.forEach(action);
		}

		private List<String> getLines() {
			try {
				StringWriter writer = new StringWriter();
				store(writer, null);
				List<String> lines = Arrays.stream(writer.toString().split("\n")).map(this::postProcess)
						.collect(Collectors.toList());
				lines.remove(0);
				return lines;
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		private String postProcess(String line) {
			int split = line.indexOf('=');
			if (split == -1) {
				return line;
			}
			String name = line.substring(0, split);
			String value = line.substring(split);
			if (name.contains(BLANK_LINE)) {
				return "";
			}
			if (name.contains(COMMENT)) {
				try {
					Properties properties = new Properties();
					properties.load(new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8)));
					return (String) properties.values().iterator().next();
				}
				catch (IOException ex) {
					throw new IllegalStateException(ex);
				}
			}
			value = value.replace("\\:", ":");
			value = value.replace("\\=", "=");
			return name + value;
		}

	}

}
