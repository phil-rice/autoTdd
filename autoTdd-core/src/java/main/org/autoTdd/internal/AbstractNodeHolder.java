package org.autoTdd.internal;

import java.util.ArrayList;
import java.util.List;

import org.autoTdd.IEngineStrategy;
import org.autoTdd.ISystemSpecification;
import org.autoTdd.builder.internal.Node;
import org.softwarefm.utilities.indent.Indent;

abstract public class AbstractNodeHolder extends EngineType {

	protected final ISystemSpecification specification;
	protected final Object defaultOutput;
	protected final IEngineStrategy engineStrategy;
	protected final Object context;

	public AbstractNodeHolder(ISystemSpecification specification) {
		super(specification.resultClass(), specification.parameters());
		this.specification = specification;
		this.engineStrategy = specification.engineStrategy();
		this.defaultOutput = specification.defaultOutput();
		this.context = engineStrategy.makeContext();
	}

	protected Node findNode(Node root, Object... inputs) {
		Node lastMatch = null;
		Node nextNode = root;
		while (nextNode != null) {
			Constraint constraint = nextNode.constraint;
			boolean match = engineStrategy.match(context, constraint, inputs);
			if (match)
				lastMatch = nextNode;
			nextNode = match ? nextNode.match : nextNode.noMatch;
		}
		return lastMatch;
	}

	protected Node findAllfalseNode(Node root) {
		Node result = root;
		while (result.noMatch != null)
			result = result.noMatch;
		return result;
	}

	public String asString(Node root) {
		StringBuilder builder = new StringBuilder();
		Indent indent = new Indent();
		add(builder, indent, root, defaultOutput);
		return builder.toString();
	}

	private void add(StringBuilder builder, Indent indent, Node node, Object defaultOutput) {
		builder.append(indent);
		builder.append("if (");
		builder.append(engineStrategy.displayBecause(indent, node.constraint));
		builder.append(")\n");
		Indent indented = indent.indent();
		if (node.match == null) {
			builder.append(indented);
			builder.append(node.constraint.getResult());
			builder.append('\n');
		} else
			add(builder, indented, node.match, node.constraint.getResult());
		builder.append(indent);
		builder.append("else\n");
		if (node.noMatch == null) {
			builder.append(indented);
			builder.append(defaultOutput);
			builder.append('\n');
		} else
			add(builder, indented, node.noMatch, defaultOutput);
	}

	protected Object resultFrom(Node node) {
		if (node != null)
			if (node.constraint != null)
				return node.constraint.getResult();
		return defaultOutput;
	}

	
}
