package org.autoTdd.engine;


public class Engine<Input, Match, Result> {

	static class Node<Input, Match, Result> {
		private Constraint<Input, Match, Result> constraint;
		private Node<Input, Match, Result> lastMatch;
		private Node<Input, Match, Result> match;
		private Node<Input, Match, Result> nomatch;

		public Node(Constraint<Input, Match, Result> constraint, Node<Input, Match, Result> lastMatch, Node<Input, Match, Result> match, Node<Input, Match, Result> nomatch) {
			this.constraint = constraint;
			this.lastMatch = lastMatch;
			this.match = match;
			this.nomatch = nomatch;
		}

		@Override
		public String toString() {
			return "Node [constraint=" + constraint + ", match=" + match + ", nomatch=" + nomatch + "]";
		}

	}

	private Node<Input, Match, Result> main;

	private Result defaultValue;

	public Engine(Result defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void add(Constraint<Input, Match, Result>... constraints) {
		for (Constraint<Input, Match, Result> constraint : constraints)
			add(constraint);
	}

	public void add(Constraint<Input, Match, Result> constraint) {
		if (main == null)
			main = new Node<Input, Match, Result>(constraint, null, null, null);
		else {
			Node<Input, Match, Result> leafNode = findNode(constraint.input);
			Match match = leafNode.constraint.match(constraint.input);
			if (match == null)
				leafNode.nomatch = new Node<Input, Match, Result>(constraint, leafNode.lastMatch, null, null);
			else
				leafNode.match = new Node<Input, Match, Result>(constraint, leafNode, null, null);
		}
	}

	private Node<Input, Match, Result> findNode(Input input) {
		Node<Input, Match, Result> node = main;
		Node<Input, Match, Result> nextNode = node;
		while (nextNode != null) {
			Constraint<Input, Match, Result> constraint = nextNode.constraint;
			Match match = constraint.match(input);
			node = nextNode;
			nextNode = match == null ? node.nomatch : node.match;
		}
		return node;
	}

	public Result defaultValue() {
		return defaultValue;
	}

	public Result apply(Input input) {
		if (main == null)
			return defaultValue;
		Node<Input, Match, Result> node = findNode(input);
		Constraint<Input, Match, Result> constraint = node.constraint;
		Match match = constraint.match(input);
		if (match == null) {
			Node<Input, Match, Result> lastMatchNode = node.lastMatch;
			if (lastMatchNode == null)
				return defaultValue;
			else
				return lastMatchNode.constraint.result(lastMatchNode.constraint.match(input));
		}
		return constraint.result(match);
	}
}