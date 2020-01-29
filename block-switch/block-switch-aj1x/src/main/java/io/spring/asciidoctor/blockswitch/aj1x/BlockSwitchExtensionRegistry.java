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

package io.spring.asciidoctor.blockswitch.aj1x;

import java.lang.reflect.Method;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.extension.DocinfoProcessor;
import org.asciidoctor.extension.spi.ExtensionRegistry;

/**
 * {@link ExtensionRegistry Registry} for the block switch extension.
 *
 * @author Andy Wilkinson
 */
public class BlockSwitchExtensionRegistry implements ExtensionRegistry {

	@Override
	public void register(Asciidoctor asciidoctor) {
		Object javaExtensionRegistry = asciidoctor.javaExtensionRegistry();
		try {
			Method docinfoProcessor = javaExtensionRegistry.getClass().getMethod("docinfoProcessor",
					DocinfoProcessor.class);
			docinfoProcessor.invoke(javaExtensionRegistry, new BlockSwitchDocinfoProcessor());
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed to register " + BlockSwitchDocinfoProcessor.class.getSimpleName());
		}
	}

}
