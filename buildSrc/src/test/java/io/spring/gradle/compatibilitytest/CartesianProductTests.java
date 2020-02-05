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

package io.spring.gradle.compatibilitytest;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CartesianProduct}.
 *
 * @author Andy Wilkinson
 */
class CartesianProductTests {

	@Test
	void oneSet() {
		List<Set<String>> list = list(set("a", "b", "c"));
		Set<List<String>> result = CartesianProduct.of(list);
		assertThat(result).containsExactly(list("a"), list("b"), list("c"));
	}

	@Test
	void twoSets() {
		Set<List<String>> result = CartesianProduct.of(list(set("a", "b"), set("1", "2", "3")));
		assertThat(result).containsExactly(list("a", "1"), list("a", "2"), list("a", "3"), list("b", "1"),
				list("b", "2"), list("b", "3"));
	}

	@Test
	void threeSets() {
		Set<List<String>> result = CartesianProduct.of(list(set("a", "b"), set("1", "2"), set("red", "blue")));
		assertThat(result).containsExactly(list("a", "1", "red"), list("a", "1", "blue"), list("a", "2", "red"),
				list("a", "2", "blue"), list("b", "1", "red"), list("b", "1", "blue"), list("b", "2", "red"),
				list("b", "2", "blue"));
	}

	@Test
	void emptySet() {
		Set<List<String>> result = CartesianProduct.of(list(set("a", "b"), set()));
		assertThat(result.isEmpty());
	}

	private Set<String> set(String... items) {
		return new LinkedHashSet<>(Arrays.asList(items));
	}

	@SafeVarargs
	private final List<Set<String>> list(Set<String>... sets) {
		return Arrays.asList(sets);
	}

	private List<String> list(String... items) {
		return Arrays.asList(items);
	}

}
