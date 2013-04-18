package org.autoTdd.engine.internal;

import java.util.ArrayList;
import java.util.List;

import org.autoTdd.IEngineSpecification;
import org.autoTdd.builder.internal.Node;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.internal.AbstractNodeHolder;

public class Engine extends AbstractNodeHolder implements IEngineAsTree {

	private final Node root;

	public Engine(IEngineSpecification specification, Node root) {
		super(specification);
		this.root = root;
	}

	@Override
	public Node getRoot() {
		return root;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Result> Result transformRaw(Object... inputs) {
		Node node = findNode(root, inputs);
		if (node == null)
			return (Result) defaultOutput;
		//TODO we could avoid this match by returning it from findNode, but that would require object creation... 
		Boolean match = engineStrategy.match(context, node.constraint, inputs);
		if (match != null && match)
			return (Result) resultFrom(node);
		else
			return (Result) resultFrom(node.lastMatch);
	}
	@Override
	public List<Node> allNodes() {
		List<Node> result = new ArrayList<Node>();
		addNodeAndChildren(result, root);
		return result;
	}
	protected void addNodeAndChildren(List<Node> result, Node node) {
		if (node != null){
			result.add(node);
			addNodeAndChildren(result, node.match);
			addNodeAndChildren(result, node.noMatch);
		}
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "\n " + asString(root);
	}


}
