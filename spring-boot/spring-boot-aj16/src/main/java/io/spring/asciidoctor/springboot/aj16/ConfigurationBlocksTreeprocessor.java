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

package io.spring.asciidoctor.springboot.aj16;

import java.util.ArrayList;
import java.util.List;

import io.spring.asciidoctor.springboot.ConfigBlock;
import io.spring.asciidoctor.springboot.ConfigBlocksGenerator;
import io.spring.asciidoctor.springboot.Logger;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;

/**
 * {@link Treeprocessor} that generates config property blocks.
 *
 * @author Phillip Webb
 */
public class ConfigurationBlocksTreeprocessor extends Treeprocessor {

	private final ConfigBlocksGenerator generator;

	ConfigurationBlocksTreeprocessor(Logger logger) {
		this.generator = new ConfigBlocksGenerator();
	}

	@Override
	public Document process(Document document) {
		process((StructuralNode) document);
		return document;
	}

	private List<StructuralNode> process(StructuralNode node) {
		if (node instanceof Block && hasAttribute(node, "configblocks")) {
			return processBlock((Block) node);
		}
		processChildren(node);
		return null;
	}

	private List<StructuralNode> processBlock(Block block) {
		String language = (String) block.getAttribute("language");
		ConfigBlock in = new ConfigBlock(block.getTitle(), language, block.getLines());
		List<StructuralNode> result = new ArrayList<>();
		this.generator.apply(in, (configData) -> result.add(createConfigDataBlock(block, configData)));
		setRoles(result);
		return result;
	}

	private void setRoles(List<StructuralNode> result) {
		for (int i = 0; i < result.size(); i++) {
			result.get(i).setAttribute("role", (i != 0) ? "secondary" : "primary", true);
		}
	}

	private StructuralNode createConfigDataBlock(Block source, ConfigBlock configData) {
		Block result = createBlock((StructuralNode) source.getParent(), source.getContext(), configData.getContent(),
				source.getAttributes());
		result.setStyle(source.getStyle());
		result.setTitle(configData.getTitle());
		result.setAttribute("language", configData.getLanguage(), true);
		return result;
	}

	private void processChildren(StructuralNode node) {
		List<StructuralNode> children = getChildren(node);
		if (children != null && !children.isEmpty()) {
			int i = 0;
			while (i < children.size()) {
				StructuralNode child = children.get(i);
				List<StructuralNode> processed = process(child);
				if (processed != null) {
					children.remove(i);
					children.addAll(i, processed);
					i += processed.size();
				}
				else {
					i++;
				}
			}
		}
	}

	private boolean hasAttribute(StructuralNode node, String attribute) {
		if (node instanceof DescriptionListEntry) {
			return false;
		}
		return node.getAttributes().containsValue(attribute);
	}

	private List<StructuralNode> getChildren(StructuralNode node) {
		if (node instanceof DescriptionListEntry) {
			return ((DescriptionListEntry) node).getDescription().getBlocks();
		}
		return node.getBlocks();
	}

}
