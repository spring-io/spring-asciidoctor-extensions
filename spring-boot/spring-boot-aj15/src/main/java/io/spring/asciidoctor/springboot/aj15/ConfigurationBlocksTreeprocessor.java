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

package io.spring.asciidoctor.springboot.aj15;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.spring.asciidoctor.springboot.ConfigBlock;
import io.spring.asciidoctor.springboot.ConfigBlocksGenerator;
import io.spring.asciidoctor.springboot.Logger;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListNode;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.internal.RubyObjectWrapper;
import org.jruby.java.proxies.RubyObjectHolderProxy;

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
		process((AbstractBlock) document);
		return document;
	}

	private List<AbstractBlock> process(AbstractBlock node) {
		if (node instanceof Block && hasAttribute(node, "configblocks")) {
			return processBlock((Block) node);
		}
		processChildren(node);
		return null;
	}

	private List<AbstractBlock> processBlock(Block block) {
		String language = (String) block.getAttr("language");
		ConfigBlock in = new ConfigBlock(block.getTitle(), language, block.lines());
		List<AbstractBlock> result = new ArrayList<>();
		this.generator.apply(in, (configData) -> result.add(createConfigDataBlock(block, configData)));
		setRoles(result);
		return result;
	}

	private void setRoles(List<AbstractBlock> result) {
		for (int i = 0; i < result.size(); i++) {
			result.get(i).setAttr("role", (i != 0) ? "secondary" : "primary", true);
		}
	}

	private AbstractBlock createConfigDataBlock(Block source, ConfigBlock configData) {
		Map<Object, Object> options = new LinkedHashMap<>();
		Block result = createBlock((AbstractBlock) source.getParent(), source.getContext(), configData.getContent(),
				source.getAttributes(), options);
		RubyObjectHolderProxy proxy = (RubyObjectHolderProxy) result;
		RubyObjectWrapper wrapper = new RubyObjectWrapper(proxy.__ruby_object());
		wrapper.setString("style", source.getStyle());
		wrapper.setString("title", source.getTitle());
		result.setAttr("language", configData.getLanguage(), true);
		return result;
	}

	private void processChildren(AbstractBlock node) {
		List<AbstractBlock> children = getChildren(node);
		if (children != null && !children.isEmpty()) {
			int i = 0;
			while (i < children.size()) {
				AbstractBlock child = children.get(i);
				List<AbstractBlock> processed = process(child);
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

	private boolean hasAttribute(AbstractBlock node, String attribute) {
		return node.getAttributes().containsValue(attribute);
	}

	private List<AbstractBlock> getChildren(AbstractBlock block) {
		if (block instanceof ListNode) {
			return null;
		}
		return block.getBlocks();
	}

}
