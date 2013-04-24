package org.autoTdd.tests;

import java.util.Arrays;

import org.autoTdd.builder.internal.Node;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.internal.Constraint;

public class TreeComparator {

	public String describeMisMatch(IEngineAsTree tree1, IEngineAsTree tree2) {
		String nodes = describeMisMatch("", tree1.getRoot(), tree2.getRoot());
		if (nodes != null)
			return nodes;
		if (notEqual(tree1.defaultOutput(), tree2.defaultOutput()))
			return "Default output mismatch: " + tree1.defaultOutput() + ", " + tree2.defaultOutput();
		return null;
	}

	private String describeMisMatch(String path, Node node1, Node node2) {
		if (node1 == null || node2 == null)
			if (node1 != node2)
				return result("[" + path + "]  One node is null, other isnt: ", node1 , node2);
			else
				return null;
		String constraintMismatch = describeMisMatch(path, node1.constraint, node2.constraint);
		if (constraintMismatch != null)
			return constraintMismatch;
		Node lastMatch1 = node1.lastMatch;
		Node lastMatch2 = node2.lastMatch;
		if (toStringsNotEqual(lastMatch1, lastMatch2)) // weak assessment but alternatives are awkward
			return result("[" + path + "] LastMatchMismatch", lastMatch1, lastMatch2);
		String trueMismatch = describeMisMatch(path + "T", node1.match, node2.match);
		if (trueMismatch != null)
			return trueMismatch;
		return describeMisMatch(path + "F", node1.noMatch, node2.noMatch);
	}

	private boolean toStringsNotEqual(Node node1, Node node2) {
		if (node1 == null || node2 == null)
			return node1 != node2;
		return !node1.toString().equals(node2.toString());
	}

	private String result(String prefix, Object obj1, Object obj2) {
		return prefix + "\n" + obj1 + "\n" + obj2;

	}

	private String describeMisMatch(String path, Constraint constraint1, Constraint constraint2) {
		String prefix = "[" + path + "] Constraints /";
		if (notEqual(constraint1.getBecause(), constraint2.getBecause()))
			return result(prefix + " because", constraint1, constraint2);
		if (notEqual(constraint1.getResult(), constraint2.getResult()))
			return result(prefix + " result", constraint1, constraint2);
		if (notEqual(Arrays.asList(constraint1.getInputs()), Arrays.asList(constraint2.getInputs())))
			return result(prefix + " inputs", constraint1, constraint2);
		return null;
	}

	private <T> boolean notEqual(T one, T two) {
		if (one == null)
			return two != null;
		return !one.equals(two);
	}

}
