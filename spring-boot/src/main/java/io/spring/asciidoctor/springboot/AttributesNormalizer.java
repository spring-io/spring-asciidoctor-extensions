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

import java.util.HashMap;
import java.util.Map;

/**
 * Normalizes the attributes passed to an {@code InlineMacroProcessor}, parsing them when
 * they have all be included in the value of the {@code text} attribute.
 *
 * @author Andy Wilkinson
 */
public final class AttributesNormalizer {

	private AttributesNormalizer() {

	}

	/**
	 * Normalizes the given {@code source} attributes.
	 * @param source source attributes
	 * @return normalized attributes
	 */
	public static Map<String, Object> normalize(Map<String, Object> source) {
		if (!source.containsKey("text")) {
			return source;
		}
		String text = source.get("text").toString();
		Map<String, Object> attributes = new HashMap<>();
		for (String component : text.split(",")) {
			String[] keyValue = component.split("=");
			int key = 1;
			if (keyValue.length == 2) {
				attributes.put(keyValue[0], keyValue[1]);
			}
			else {
				attributes.put(Integer.toString(key++), keyValue[0]);
			}
		}
		return attributes;
	}

}
