/*
 * Copyright 2014-2019 the original author or authors.
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Configuration properties read from {@code META-INF/spring-configuration-metadata.json}.
 *
 * @author Andy Wilkinson
 */
public final class ConfigurationProperties {

	private static final Type MAP_TYPE = new MapTypeToken().getType();

	private final Map<String, ConfigurationProperty> properties;

	private ConfigurationProperties(Collection<ConfigurationProperty> properties) {
		this.properties = properties.stream()
				.collect(Collectors.toMap(ConfigurationProperty::getName, Function.identity()));
	}

	public int size() {
		return this.properties.size();
	}

	public ConfigurationProperty find(String name) {
		ConfigurationProperty exactMatch = this.properties.get(name);
		if (exactMatch != null) {
			return exactMatch;
		}
		int index = name.lastIndexOf('[');
		if (index >= 0) {
			return find(name.substring(0, index));
		}
		return findMapAncestor(name);
	}

	private ConfigurationProperty findMapAncestor(String name) {
		int index = name.lastIndexOf('.');
		if (index > -1) {
			String parent = name.substring(0, index);
			ConfigurationProperty parentProperty = this.properties.get(parent);
			if (parentProperty != null && parentProperty.isMap()) {
				return parentProperty;
			}
			return findMapAncestor(parent);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static ConfigurationProperties fromClasspath(ClassLoader classLoader) {
		List<ConfigurationProperty> configurationProperties = new ArrayList<>();
		try {
			Enumeration<URL> resources = classLoader.getResources("META-INF/spring-configuration-metadata.json");
			Gson gson = new GsonBuilder().create();
			while (resources.hasMoreElements()) {
				try (InputStream stream = resources.nextElement().openStream()) {
					Map<String, Object> json = gson.fromJson(new InputStreamReader(stream), MAP_TYPE);
					List<Map<String, Object>> properties = (List<Map<String, Object>>) json.get("properties");
					for (Map<String, Object> property : properties) {
						String name = (String) property.get("name");
						configurationProperties.add(new ConfigurationProperty(name, (String) property.get("type"),
								property.containsKey("deprecation")));
					}
				}
			}
			return new ConfigurationProperties(configurationProperties);
		}
		catch (IOException ex) {
			throw new RuntimeException("Failed to load configuration metadata", ex);
		}
	}

	private static final class MapTypeToken extends TypeToken<Map<String, Object>> {

	}

}
