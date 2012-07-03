/* This file is part of AutoTDD

/* AutoTDD is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
/* AutoTDD is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
/* You should have received a copy of the GNU General Public License along with AutoTDD. If not, see <http://www.gnu.org/licenses/> */
package org.autoTdd;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.concurrent.atomic.AtomicInteger;

public class AutoTdd<Input, Result> {
	class Node {
		final Constraint<Input, Result> constraint;
		Node noMatch;
		Node match;
		final Node lastMatch;

		public Node(Constraint<Input, Result> constraint, Node lastMatch, Node noMatch, Node match) {
			this.constraint = constraint;
			this.lastMatch = lastMatch;
			this.noMatch = noMatch;
			this.match = match;
		}
	}

	private Node main;
	private final Result defaultValue;

	public AutoTdd(Result defaultValue, Constraint<Input, Result>... constraints) {
		this.defaultValue = defaultValue;
		addDefaultConstraints();
		add(constraints);

	}

	protected void addDefaultConstraints() {
	}

	public void add(Constraint<Input, Result>... constraints) {
		for (Constraint<Input, Result> constraint : constraints)
			add(constraint);
	}

	public void add(Constraint<Input, Result> constraint) {
		if (main == null)
			main = new Node(constraint, null, null, null);
		else {
			Node leafNode = findNode(constraint.input);
			if (leafNode.constraint.result.equals(constraint.result))
				;// no action needed as new one is correctly processed.... this should probably be an option
			else {
				boolean lastMatchMatch = match(constraint, leafNode.constraint.input);
				if (lastMatchMatch)
					throw new ConstraintConflictException(leafNode.constraint, constraint);
				boolean match = match(leafNode.constraint, constraint.input);
				if (match)
					leafNode.match = new Node(constraint, leafNode, null, null);
				else
					leafNode.noMatch = new Node(constraint, leafNode.lastMatch, null, null);
			}
		}
	}

	private boolean match(Constraint<Input, Result> constraint, Input input) {
		Binding binding = new Binding();
		binding.setVariable("input", input);
		GroovyShell shell = new GroovyShell(binding);
		Object value = shell.evaluate(constraint.why);
		if (value == null)
			return false;
		if (!(value instanceof Boolean))
			throw new IllegalStateException("Should be boolean is " + value.getClass() + ": " + value);
		return (Boolean) value;
	}

	public Result apply(Input input) {
		Node node = findNode(input);
		if (node == null)
			return defaultValue;
		Boolean match = match(node.constraint, input);
		if (match != null && match)
			return node.constraint.result;
		else
			return node.lastMatch.constraint.result;
	}

	private Node findNode(Input input) {
		Node node = main;
		Node nextNode = node;
		while (nextNode != null) {
			Constraint<Input, Result> constraint = nextNode.constraint;
			boolean match = match(constraint, input);
			node = nextNode;
			nextNode = match ? node.match : node.noMatch;
		}
		return node;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		AtomicInteger indent = new AtomicInteger();
		Node node = main;
		add(builder, indent, node);
		indent.incrementAndGet();
		msg(builder, indent, defaultValue);
		return builder.toString();
	}

	private void add(StringBuilder builder, AtomicInteger indent, Node node) {
		addWhy(builder, indent, node.constraint.why);
		indent.incrementAndGet();
		addThen(builder, indent, node.match);
		indent.incrementAndGet();
		indent.incrementAndGet();
		msg(builder, indent, node.constraint.result);
		indent.decrementAndGet();
		indent.decrementAndGet();
		indent.decrementAndGet();
		addElse(builder, indent, node.noMatch, node.constraint.result);
	}

	private void addThen(StringBuilder builder, AtomicInteger indent, Node node) {
		if (node != null)
			add(builder, indent, node);
	}

	private void addElse(StringBuilder builder, AtomicInteger indent, Node node, Object defaultValue) {
		msg(builder, indent, "else");
		if (node == null)
			return;
		indent.incrementAndGet();
		indent.incrementAndGet();
		add(builder, indent, node);
		indent.decrementAndGet();
		indent.decrementAndGet();
	}

	private void addWhy(StringBuilder builder, AtomicInteger indent, String why) {
		msg(builder, indent, "if (" + why + ")");
	}

	private void msg(StringBuilder builder, AtomicInteger indent, Object message) {
		indent(builder, indent.get());
		builder.append(message);
		builder.append("\n");

	}

	private void indent(StringBuilder builder, int indent) {
		builder.append("                         ".substring(0, indent));
	}
}