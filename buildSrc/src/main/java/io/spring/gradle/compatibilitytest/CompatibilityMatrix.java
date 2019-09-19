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
import java.util.List;
import java.util.Set;

/**
 * Matrix of {@link DependencyVersion dependency versions} with which the project must be
 * compatible.
 *
 * @author Andy Wilkinson
 */
class CompatibilityMatrix {

	private final List<Set<DependencyVersion>> entries = new ArrayList<>();

	void add(Set<DependencyVersion> dependencyVersions) {
		this.entries.add(dependencyVersions);
	}

	List<Set<DependencyVersion>> getEntries() {
		return this.entries;
	}

	/**
	 * A version of a dependency with which the project must be compatible.
	 */
	static final class DependencyVersion {

		private final String name;

		private final String groupId;

		private final String artifactId;

		private final String version;

		DependencyVersion(String name, String groupId, String artifactId, String version) {
			this.name = name;
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}

		String getGroupId() {
			return this.groupId;
		}

		String getArtifactId() {
			return this.artifactId;
		}

		String getVersion() {
			return this.version;
		}

		String getIdentifier() {
			return this.name.toLowerCase().replace(' ', '_') + "_" + this.version;
		}

		String getDescription() {
			return this.name + " " + this.version;
		}

	}

}
