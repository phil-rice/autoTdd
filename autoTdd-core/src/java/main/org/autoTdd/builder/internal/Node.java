package org.autoTdd.builder.internal;

import org.autoTdd.internal.Constraint;

public class Node {
	public  Constraint constraint;
	public Node noMatch;
	public Node match;
	public final Node lastMatch;

	public Node(Constraint constraint, Node lastMatch, Node noMatch, Node match) {
		this.constraint = constraint;
		this.lastMatch = lastMatch;
		this.noMatch = noMatch;
		this.match = match;
	}
	
	public boolean isLeaf(){
		return match == null && noMatch == null;
	}

	@Override
	public String toString() {
		return "Node [constraint=" + constraint + "]";
	}

}