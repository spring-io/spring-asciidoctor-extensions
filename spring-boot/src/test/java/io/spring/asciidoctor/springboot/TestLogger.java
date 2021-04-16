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

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.AssertProvider;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;

/**
 * Test {@link Logger} implementation.
 *
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
public class TestLogger implements Logger, AssertProvider<TestLogger.Assert> {

	private final List<String> warnMessages = new ArrayList<>();

	private final List<String> debugMessages = new ArrayList<>();

	@Override
	public void warn(String message) {
		this.warnMessages.add(message);
	}

	@Override
	public void debug(String message) {
		this.debugMessages.add(message);
	}

	@Override
	public Assert assertThat() {
		return new Assert();
	}

	public class Assert {

		public ListAssert<String> warnMessages() {
			return Assertions.assertThat(TestLogger.this.warnMessages);
		}

		public ListAssert<String> debugMessages() {
			return Assertions.assertThat(TestLogger.this.debugMessages);
		}

	}

}
