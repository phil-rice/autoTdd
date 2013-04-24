package org.autoTdd.tests;

import junit.framework.TestCase;

import org.autoTdd.builder.internal.Node;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.engine.IRooted;
import org.softwarefm.utilities.tests.Tests;

public class AsciiArtBuilderTest extends TestCase {

	private AsciiArtBuilder builder = new AsciiArtBuilder();

	public void testOneIfThenWithNoInputs() {
		register();
		IRooted tree = builder.build("if a then x else y");
		Node root = tree.getRoot();
		assertNull(root.lastMatch);
		assertNull(root.match);
		assertNull(root.noMatch);
		assertEquals("A", root.constraint.getBecause());
		Tests.assertArrayEquals(root.constraint.getInputs());
		assertEquals("X", root.constraint.getResult());
	}

	public void testOneIfThenWithOneInputs() {
		register();
		IEngineAsTree tree = builder.build("if a/1 then x else y");
		Node root = tree.getRoot();
		assertNull(root.lastMatch);
		assertNull(root.match);
		assertNull(root.noMatch);
		assertEquals("A", root.constraint.getBecause());
		Tests.assertArrayEquals(root.constraint.getInputs(), "One");
		assertEquals("X", root.constraint.getResult());
		assertEquals("Y", tree.defaultOutput());
	}

	public void testOneIfThenWithTwoInputs() {
		register();
		IEngineAsTree tree = builder.build("if a/1,2 then x else y");
		Node root = tree.getRoot();
		assertNull(root.lastMatch);
		assertNull(root.match);
		assertNull(root.noMatch);
		assertEquals("A", root.constraint.getBecause());
		Tests.assertArrayEquals(root.constraint.getInputs(), "One", "Two");
		assertEquals("X", root.constraint.getResult());
		assertEquals("Y", tree.defaultOutput());
	}

	public void testNestedIfInThenClause() {
		register();
		IEngineAsTree tree = builder.build("if a/1,2 if b/2,3 then x else y else z");
		Node root = tree.getRoot();
		assertNull(root.lastMatch);
		{
			Node nested = root.match;
			assertNull(nested.match);
			assertNull(nested.noMatch);
			assertEquals(root, nested.lastMatch);
			assertEquals("B", nested.constraint.getBecause());
			Tests.assertArrayEquals(nested.constraint.getInputs(), "Two", "Three");
			assertEquals("X", nested.constraint.getResult());

		}
		assertNull(root.noMatch);
		assertEquals("A", root.constraint.getBecause());
		Tests.assertArrayEquals(root.constraint.getInputs(), "One", "Two");
		assertEquals("Y", root.constraint.getResult());
		assertEquals("Z", tree.defaultOutput());
	}

	public void testNestedIfInElseClause() {
		register();
		IEngineAsTree tree = builder.build("if a/1,2 then x else if b/2,3 then y else z");
		Node root = tree.getRoot();
		assertNull(root.lastMatch);
		assertNull(root.match);
		{
			Node nested = root.noMatch;
			assertNull(nested.match);
			assertNull(nested.noMatch);
			assertEquals(null, nested.lastMatch);
			assertEquals("B", nested.constraint.getBecause());
			Tests.assertArrayEquals(nested.constraint.getInputs(), "Two", "Three");
			assertEquals("Y", nested.constraint.getResult());

		}
		assertEquals("A", root.constraint.getBecause());
		Tests.assertArrayEquals(root.constraint.getInputs(), "One", "Two");
		assertEquals("X", root.constraint.getResult());
		assertEquals("Z", tree.defaultOutput());
	}

	public void testTwoNestedsIfClauses() {
		register();
		IEngineAsTree tree =  builder.build("if a/1,2 if b/2,3 then x else y else if b/3,4 then z else w");
		Node root = tree.getRoot();
		assertNull(root.lastMatch);
		{
			Node nested = root.match;
			assertNull(nested.match);
			assertNull(nested.noMatch);
			assertEquals(root, nested.lastMatch);
			assertEquals("B", nested.constraint.getBecause());
			Tests.assertArrayEquals(nested.constraint.getInputs(), "Two", "Three");
			assertEquals("X", nested.constraint.getResult());

		}
		{
			Node nested = root.noMatch;
			assertNull(nested.match);
			assertNull(nested.noMatch);
			assertEquals(null, nested.lastMatch);
			assertEquals("B", nested.constraint.getBecause());
			Tests.assertArrayEquals(nested.constraint.getInputs(), "Three", "Four");
			assertEquals("Z", nested.constraint.getResult());

		}
		assertEquals("A", root.constraint.getBecause());
		Tests.assertArrayEquals(root.constraint.getInputs(), "One", "Two");
		assertEquals("Y", root.constraint.getResult());
		assertEquals("W", tree.defaultOutput());
	}

	private void register() {
		builder.registerBecause("a", "A");
		builder.registerBecause("b", "B");
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
