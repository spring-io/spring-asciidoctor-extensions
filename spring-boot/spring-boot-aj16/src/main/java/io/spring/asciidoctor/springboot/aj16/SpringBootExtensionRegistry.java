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

package io.spring.asciidoctor.springboot.aj16;

import io.spring.asciidoctor.springboot.Logger;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.asciidoctor.extension.spi.ExtensionRegistry;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

/**
 * {@link ExtensionRegistry Registry} for the Spring Boot extension.
 *
 * @author Andy Wilkinson
 */
public class SpringBootExtensionRegistry implements ExtensionRegistry {

	@Override
	public void register(Asciidoctor asciidoctor) {
		if (!asciidoctorJ16()) {
			return;
		}
		JavaExtensionRegistry registry = asciidoctor.javaExtensionRegistry();
		Logger logger = new LogHandlerLoggerAdapter((LogHandler) asciidoctor);
		registry.inlineMacro(new ConfigurationPropertyInlineMacroProcessor(logger));
		registry.treeprocessor(new ConfigurationPropertiesTreeprocessor(logger));
		registry.treeprocessor(new ConfigurationBlocksTreeprocessor(logger));
	}

	private boolean asciidoctorJ16() {
		try {
			return Class.forName("org.asciidoctor.extension.JavaExtensionRegistry").isInterface();
		}
		catch (Throwable ex) {
			return false;
		}
	}

	private static final class LogHandlerLoggerAdapter implements Logger {

		private final LogHandler logHandler;

		private LogHandlerLoggerAdapter(LogHandler logHandler) {
			this.logHandler = logHandler;
		}

		@Override
		public void warn(String message) {
			this.logHandler.log(new LogRecord(Severity.WARN, null, message, null, null));
		}

		@Override
		public void debug(String message) {
			this.logHandler.log(new LogRecord(Severity.DEBUG, null, message, null, null));
		}

	}

}
