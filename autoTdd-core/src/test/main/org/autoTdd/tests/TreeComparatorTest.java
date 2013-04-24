package org.autoTdd.tests;

import junit.framework.TestCase;

import org.autoTdd.engine.IEngineAsTree;

public class TreeComparatorTest extends TestCase {

	private final static TreeComparator treeComparator = new TreeComparator();
	private AsciiArtBuilder builder;

	public void testAgainstSingleNodes() {
		compareTrees("if a then x else y", "if a then x else y", null);
		compareTrees("if a then x else y", "if b then x else y", //
				"[] Constraints / because\nConstraint [result=X, because=A, inputs=[]]\nConstraint [result=X, because=B, inputs=[]]");
		compareTrees("if a then x else y", "if a then x else z", //
				"Default output mismatch: Y, Z");
		compareTrees("if a then x else y", "if a then z else y", //
				"[] Constraints / result\nConstraint [result=X, because=A, inputs=[]]\nConstraint [result=Z, because=A, inputs=[]]");
	}

	public void testNestedThenPath() {
		compareTrees("if a if b then x else y else z", "if a if b then x else y else z", null);
		compareTrees("if a if b then x else y else z", "if a if b then x else w else z", //
				"[] Constraints / result\nConstraint [result=Y, because=A, inputs=[]]\nConstraint [result=W, because=A, inputs=[]]");
		compareTrees("if a if b then x else y else z", "if a if b then w else y else z", //
				"[T] Constraints / result\nConstraint [result=X, because=B, inputs=[]]\nConstraint [result=W, because=B, inputs=[]]");
		compareTrees("if a if b then x else y else z", "if a if c then x else y else z", //
				"[T] Constraints / because\nConstraint [result=X, because=B, inputs=[]]\nConstraint [result=X, because=C, inputs=[]]");
	}
	public void testNestedElsePath() {
		compareTrees("if a then x else if b then y else z", "if a then x else if b then y else z", null);
		compareTrees("if a then x else if b then y else z", "if a then x else if c then y else z", //
				"[F] Constraints / because\nConstraint [result=Y, because=B, inputs=[]]\nConstraint [result=Y, because=C, inputs=[]]");
		compareTrees("if a then x else if b then y else z", "if a then w else if b then y else z", //
				"[] Constraints / result\nConstraint [result=X, because=A, inputs=[]]\nConstraint [result=W, because=A, inputs=[]]");
		compareTrees("if a then x else if b then y else z", "if a then x else if b then w else z", //
				"[F] Constraints / result\nConstraint [result=Y, because=B, inputs=[]]\nConstraint [result=W, because=B, inputs=[]]");
	}

	private void compareTrees(String raw1, String raw2, String expected) {
		IEngineAsTree tree1 = builder.build(raw1);
		IEngineAsTree tree2 = builder.build(raw2);
		assertEquals(expected, treeComparator.describeMisMatch(tree1, tree2));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		builder = new AsciiArtBuilder();
		builder.registerBecause("a", "A");
		builder.registerBecause("b", "B");
		builder.registerBecause("c", "C");
		builder.registerInput("1", "One");
		builder.registerInput("2", "Two");
		builder.registerInput("3", "Three");
		builder.registerInput("4", "Four");
		builder.registerResult("w", "W");
		builder.registerResult("x", "X");
		builder.registerResult("y", "Y");
		builder.registerResult("z", "Z");
	}
}
