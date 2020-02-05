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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.artifacts.ModuleVersionSelector;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;

import io.spring.gradle.compatibilitytest.CompatibilityMatrix.DependencyVersion;

/**
 * Gradle plugin for compatibility testing.
 *
 * @author Andy Wilkinson
 */
public class CompatibilityTestPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		CompatibilityMatrix testMatrix = new CompatibilityMatrix();
		CompatibilityTestExtension extension = new CompatibilityTestExtension(testMatrix);
		project.getExtensions().add("compatibilityTest", extension);
		project.afterEvaluate((evaluated) -> configure(project, testMatrix, extension));
	}

	private void configure(Project project, CompatibilityMatrix testMatrix, CompatibilityTestExtension extension) {
		List<Set<DependencyVersion>> matrixEntries = testMatrix.getEntries();
		if (matrixEntries.isEmpty()) {
			return;
		}
		CartesianProduct.of(matrixEntries)
				.forEach((dependencyVersions) -> configureTestTask(project, dependencyVersions, extension));
	}

	private void configureTestTask(Project project, List<DependencyVersion> dependencyVersions,
			CompatibilityTestExtension extension) {
		String identifier = dependencyVersions.stream().map(DependencyVersion::getIdentifier)
				.collect(Collectors.joining("_"));
		Test compatibilityTest = project.getTasks().create("compatibilityTest_" + identifier, Test.class,
				(task) -> configureMatrixTestTask(project, task, identifier, dependencyVersions));
		if (extension.isUseJUnitPlatform()) {
			compatibilityTest.useJUnitPlatform();
		}
		project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(compatibilityTest);
	}

	private void configureMatrixTestTask(Project project, Test compatibilityTest, String identifier,
			List<DependencyVersion> dependencyVersions) {
		compatibilityTest.setDescription("Runs the unit tests with "
				+ dependencyVersions.stream().map(DependencyVersion::getDescription).collect(Collectors.joining(", ")));
		compatibilityTest.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
		SourceSetContainer sourceSets = project.getConvention().findPlugin(JavaPluginConvention.class).getSourceSets();
		SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
		String runtimeClasspathConfigurationName = testSourceSet.getRuntimeClasspathConfigurationName();
		Configuration configuration = project.getConfigurations()
				.create(runtimeClasspathConfigurationName + "_" + identifier);
		configuration.extendsFrom(project.getConfigurations().getByName(runtimeClasspathConfigurationName));
		configuration.getResolutionStrategy()
				.eachDependency((details) -> dependencyVersions.stream()
						.filter((dependencyVersion) -> matches(dependencyVersion, details))
						.forEach((dependencyVersion) -> details.useVersion(dependencyVersion.getVersion())));
		compatibilityTest.setClasspath(project.files(testSourceSet.getOutput(),
				sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput(), configuration));
	}

	private boolean matches(DependencyVersion dependencyVersion, DependencyResolveDetails details) {
		ModuleVersionSelector selector = details.getRequested();
		return dependencyVersion.getGroupId().equals(selector.getGroup()) && (dependencyVersion.getArtifactId() == null
				|| dependencyVersion.getArtifactId().equals(selector.getName()));
	}

}
