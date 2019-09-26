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
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * A validator for references to Spring Boot configuration properties. Does not refer to
 * any AsciidoctorJ types to avoid problems caused by breaking API changes.
 *
 * @author Andy Wilkinson
 */
public class ConfigurationPropertyValidator {

	private final ConfigurationProperties configurationProperties = ConfigurationProperties
			.fromClasspath(getClass().getClassLoader());

	private final Logger logger;

	public ConfigurationPropertyValidator(Logger logger) {
		this.logger = logger;
	}

	public String validateProperty(String propertyName) {
		return validateProperty(propertyName, Collections.emptyList());
	}

	public String validateProperty(String propertyName, Collection<Object> attributes) {
		Result result = doValidateProperty(propertyName, attributes);
		if (result.getOutcome() == Outcome.VALIDATED) {
			this.logger.debug(result.getMessage());
		}
		else {
			this.logger.warn(result.getMessage());
		}
		return propertyName;
	}

	private Result doValidateProperty(String propertyName, Collection<Object> attributes) {
		ConfigurationProperty property = this.configurationProperties.find(propertyName);
		Outcome outcome = null;
		if (property == null) {
			outcome = Outcome.NOT_FOUND;
		}
		else {
			boolean deprecatedExcepted = attributes.contains("deprecated");
			if (property.isDeprecated() && !deprecatedExcepted) {
				outcome = Outcome.DEPRECATED;
			}
			else if (!property.isDeprecated() && deprecatedExcepted) {
				outcome = Outcome.NOT_DEPRECATED;
			}
			else {
				outcome = Outcome.VALIDATED;
			}
		}
		return new Result(propertyName, outcome);
	}

	public void validateProperties(Object content) {
		if (!(content instanceof String)) {
			return;
		}
		try {
			Properties properties = new Properties();
			properties.load(new StringReader((String) content));
			for (String propertyName : properties.stringPropertyNames()) {
				Result result = doValidateProperty(propertyName, Collections.emptyList());
				if (result.getOutcome() != Outcome.NOT_FOUND) {
					this.logger.debug(result.getMessage());
				}
				else {
					this.logger.warn(result.getMessage());
				}
			}
		}
		catch (IOException ex) {
			this.logger.warn("Failed to load properties: " + ex.getMessage());
		}
	}

	private enum Outcome {

		/**
		 * Configuration property was successfully validated.
		 */
		VALIDATED("Configuration property '%s' was successfully validated."),

		/**
		 * Configuration property was not found in the available metadata.
		 */
		NOT_FOUND("Configuration property '%s' not found."),

		/**
		 * Configuration property was deprecated when it was not expected to be.
		 */
		DEPRECATED("Configuration property '%s' is deprecated."),

		/**
		 * Configuration property was not deprecated when it was expected to be.
		 */
		NOT_DEPRECATED("Configuration property '%s' is not deprecated.");

		private String messageFormat;

		Outcome(String messageFormat) {
			this.messageFormat = messageFormat;
		}

		private String formatMessage(String propertyName) {
			return String.format(this.messageFormat, propertyName);
		}

	}

	private static final class Result {

		private final Outcome outcome;

		private final String message;

		private Result(String propertyName, Outcome outcome) {
			this.outcome = outcome;
			this.message = outcome.formatMessage(propertyName);
		}

		private Outcome getOutcome() {
			return this.outcome;
		}

		private String getMessage() {
			return this.message;
		}

	}

}
