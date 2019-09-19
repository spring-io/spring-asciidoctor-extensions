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

package io.spring.gradle.compatibilitytest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class for calculating cartesian products.
 *
 * @author Andy Wilkinson
 */
final class CartesianProduct {

	private CartesianProduct() {

	}

	static <E> Set<List<E>> of(List<? extends Set<? extends E>> sets) {
		return cartesianProduct(sets, 0);
	}

	private static <E> Set<List<E>> cartesianProduct(List<? extends Set<? extends E>> sets, int index) {
		Set<List<E>> result = new LinkedHashSet<>();
		if (index == sets.size()) {
			result.add(new ArrayList<>());
		}
		else {
			for (E item : sets.get(index)) {
				for (List<E> product : cartesianProduct(sets, index + 1)) {
					product.add(0, item);
					result.add(product);
				}
			}
		}
		return result;
	}

}
