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

package io.spring.asciidoctor.springboot.aj2x;

import java.util.HashMap;
import java.util.Map;

import io.spring.asciidoctor.springboot.ConfigurationPropertyValidator;
import io.spring.asciidoctor.springboot.Logger;
import io.spring.asciidoctor.springboot.ValidationSettings;
import io.spring.asciidoctor.springboot.ValidationSettings.Format;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;

/**
 * An {@link InlineMacroProcessor} for referencing Spring Boot configuration properties.
 *
 * @author Andy Wilkinson
 */
@ContentModel(ContentModel.ATTRIBUTES)
class ConfigurationPropertyInlineMacroProcessor extends InlineMacroProcessor {

	private final ConfigurationPropertyValidator validator;

	ConfigurationPropertyInlineMacroProcessor(Logger logger) {
		super("configprop");
		this.validator = new ConfigurationPropertyValidator(logger);
	}

	@Override
	public Object process(ContentNode parent, String propertyName, Map<String, Object> attributes) {
		Map<String, Object> options = new HashMap<>();
		options.put("type", ":monospaced");
		String validated = this.validator.validateProperty(propertyName, getSettings(attributes));
		return createPhraseNode(parent, "quoted", validated, attributes, options);
	}

	private ValidationSettings getSettings(Map<String, Object> attributes) {
		boolean deprecated = attributes.containsValue("deprecated");
		Format format = "envvar".equals(attributes.get("format")) ? Format.ENVIRONMENT_VARIABLE : null;
		return new ValidationSettings(deprecated, format);
	}

}
