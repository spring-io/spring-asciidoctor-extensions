/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.asciidoctor;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CodeBlockSwitchPostprocessor}.
 *
 * @author Andy Wilkinson
 */
public class CodeBlockSwitchPostProcessorTests {

	@Test
	public void postProcessorIsApplied() {
		Options options = new Options();
		options.setHeaderFooter(true);
		String converted = Asciidoctor.Factory.create().convert(String.format("test"), options);
		assertThat(converted).contains(".switch--item.selected");
		assertThat(converted).contains("function addBlockSwitches()");
	}

}
