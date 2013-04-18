package org.autoTdd.schengen;

import junit.framework.TestCase;

import org.autoTdd.ISystem;
import org.autoTdd.builder.internal.Node;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.jbehave.JBehaveSessionManager;
import org.autoTdd.jbehave.internal.BecauseForConstraint;
import org.softwarefm.utilities.tests.Tests;

public class ICanCrossTheBorderTest extends TestCase {

	private ISystem system;

	public void testSystemNames() {
		Tests.assertListEquals(system.engineNames(), "I assess the person");
	}

	public void testBuilds() {
		IEngineAsTree tree = system.tree("I assess the person");
		assertEquals(2, tree.allNodes().size());
		Node deborahNode = tree.getRoot();
		assertEquals("I said so", getBecauseName(deborahNode));
		Node franciscoNode = deborahNode.match;
		assertEquals("the person has a lot of money", getBecauseName(franciscoNode));
	}

	public void testTransformingDeborah() {
		Node deborahNode = system.tree("I assess the person").getRoot();
		Object actual = system.tree("I assess the person").transformRaw(deborahNode.constraint.getInputs());
		assertEquals("allowed", actual.toString());
	}

	public void testTransformingFrancisco() {
		IEngineAsTree tree = system.tree("I assess the person");
		Node franciscoNode = tree.getRoot().match;
		Object[] inputs = franciscoNode.constraint.getInputs();
		Object actual = tree.transformRaw(inputs);
		assertEquals("detained", actual.toString());
	}

	public void testTransforming() {
		for (String systemName : system.engineNames()) {
			IEngineAsTree tree = system.tree(systemName);
			for (Node node : tree.allNodes()) {
				Object expected = node.constraint.getResult();
				Object actual = tree.transformRaw(node.constraint.getInputs());
				assertEquals(systemName + "." + getBecauseName(node), expected.toString(), actual.toString());
			}
		}
	}

	private String getBecauseName(Node node) {
		BecauseForConstraint becauseForConstraint = (BecauseForConstraint) node.constraint.getBecause();
		return becauseForConstraint.becauseAnnotation.value();

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		system = JBehaveSessionManager.makeEnginesFor(ICanCrossTheBorder.class);
	}
}
