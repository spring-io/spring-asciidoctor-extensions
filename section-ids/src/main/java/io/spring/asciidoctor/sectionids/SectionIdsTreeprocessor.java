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

package io.spring.asciidoctor.sectionids;

import java.util.List;
import java.util.regex.Pattern;

import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
import org.asciidoctor.log.Severity;

/**
 * {@link Treeprocessor} that verifies section IDs.
 *
 * @author Andy Wilkinson
 */
public class SectionIdsTreeprocessor extends Treeprocessor {

	private static final Pattern TAIL_PATTERN = Pattern.compile("^[a-z0-9]+(?:-[a-z0-9]+)*$");

	// Allow . as well as - to support single- and multi-page documentation
	private static final Pattern TOP_LEVEL_PATTERN = Pattern.compile("^[a-z][a-z0-9]+(?:(-|\\.)[a-z0-9]+)*$");

	private final LogHandler log;

	SectionIdsTreeprocessor(LogHandler log) {
		this.log = log;
	}

	@Override
	public Document process(Document document) {
		process(document, false);
		return document;
	}

	private void process(StructuralNode structuralNode, boolean illegalAncestorId) {
		if (structuralNode instanceof Section) {
			Section parent = findParentSection(structuralNode);
			Section section = (Section) structuralNode;
			String id = section.getId();
			if (parent != null) {
				if (illegalAncestorId) {
					this.log.log(new LogRecord(Severity.INFO,
							"Section with ID '" + id + "' has an ancestor with an illegal ID."));
				}
				else if (!id.startsWith(parent.getId() + ".")) {
					this.log.log(new LogRecord(Severity.WARN,
							"Section ID '" + id + "' should start with '" + parent.getId() + ".'"));
					illegalAncestorId = true;
				}
				else {
					String tail = id.substring(parent.getId().length() + 1);
					if (!TAIL_PATTERN.matcher(tail).matches()) {
						this.log.log(new LogRecord(Severity.WARN,
								"'" + tail + "' tail of section ID '" + section.getId() + "' should use kebab-case"));
					}
				}
			}
			else if (!TOP_LEVEL_PATTERN.matcher(id).matches()) {
				this.log.log(new LogRecord(Severity.WARN, "Top-level section ID '" + id + "' should use kebab-case"));
			}
		}
		List<StructuralNode> children = getChildren(structuralNode);
		if (children != null) {
			for (StructuralNode child : children) {
				process(child, illegalAncestorId);
			}
		}
	}

	private Section findParentSection(ContentNode contentNode) {
		contentNode = contentNode.getParent();
		while (contentNode != null) {
			if (contentNode instanceof Section) {
				return ((Section) contentNode);
			}
			contentNode = contentNode.getParent();
		}
		return null;
	}

	private List<StructuralNode> getChildren(StructuralNode structuralNode) {
		if (structuralNode instanceof DescriptionListEntry) {
			return ((DescriptionListEntry) structuralNode).getDescription().getBlocks();
		}
		return structuralNode.getBlocks();
	}

}
