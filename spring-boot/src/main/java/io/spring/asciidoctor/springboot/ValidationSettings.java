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

import java.util.Locale;
import java.util.function.Function;

/**
 * Settings for use during property validation.
 *
 * @author Andy Wilkinson
 */
public class ValidationSettings {

	static final ValidationSettings DEFAULT = new ValidationSettings(false, Format.CANONICAL);

	private final boolean deprecated;

	private final Format format;

	public ValidationSettings(boolean deprecated, Format format) {
		this.deprecated = deprecated;
		this.format = (format != null) ? format : DEFAULT.getFormat();
	}

	public boolean isDeprecated() {
		return this.deprecated;
	}

	public Format getFormat() {
		return this.format;
	}

	public enum Format implements Function<String, String> {

		/**
		 * Property should be formatted in lower-case with {@code -} separators.
		 */
		CANONICAL(Function.identity()),

		/**
		 * Property should be formatted in upper-case with {@code _} separators.
		 */
		ENVIRONMENT_VARIABLE(Format::canonicalToEnvironmentVariable);

		private final Function<String, String> formatter;

		Format(Function<String, String> formatter) {
			this.formatter = formatter;
		}

		@Override
		public String apply(String propertyName) {
			return this.formatter.apply(propertyName);
		}

		private static String canonicalToEnvironmentVariable(String canonical) {
			return canonical.replace('.', '_').replace("-", "").toUpperCase(Locale.ENGLISH);
		}

	}

}
