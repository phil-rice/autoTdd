package org.autoTdd.schengen;

import junit.framework.TestCase;

import org.autoTdd.ISystem;
import org.autoTdd.builder.internal.Node;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.jbehave.JBehaveSessionManager;
import org.autoTdd.jbehave.exceptions.IllegalCaseException;
import org.autoTdd.jbehave.internal.BecauseForConstraint;
import org.softwarefm.utilities.tests.Tests;

@SuppressWarnings("unchecked")
public class ICanCrossTheBorderTest extends TestCase {

	public void testSystemNames() {
		ISystem system = JBehaveSessionManager.makeEnginesFor(ICanCrossTheBorder.class);
		Tests.assertListEquals(system.engineNames(), "I assess the person");
	}

	public void testBuilds() {
		ISystem system = JBehaveSessionManager.makeEnginesFor(ICanCrossTheBorder.class);
		IEngineAsTree tree = system.tree("I assess the person");
		assertEquals(3, tree.allNodes().size());
		Node deborahNode = tree.getRoot();
		assertEquals("I said so", getBecauseName(deborahNode));
		Node franciscoNode = tree.getRoot().match.noMatch;
		assertEquals("the person has a lot of money", getBecauseName(franciscoNode));
		Node garyNode = tree.getRoot().match.noMatch;
		assertEquals("the person has a lot of money", getBecauseName(garyNode));
	}

	public void testTransformingDeborah() {
		ISystem system = JBehaveSessionManager.makeEnginesFor(ICanCrossTheBorder.class);
		Node deborahNode = system.tree("I assess the person").getRoot();
		Object actual = system.tree("I assess the person").transformRaw(deborahNode.constraint.getInputs());
		assertEquals("allowed", actual.toString());
	}

	public void testTransformingFrancisco() {
		ISystem system = JBehaveSessionManager.makeEnginesFor(ICanCrossTheBorder.class);
		IEngineAsTree tree = system.tree("I assess the person");
		Node franciscoNode = tree.getRoot().match.noMatch;
		assertEquals("Francisco", ((BecauseForConstraint) franciscoNode.constraint.getBecause()).called);
		assertEquals("Francisco", ((Person) franciscoNode.constraint.getInputs()[1]).name);
		Object[] inputs = franciscoNode.constraint.getInputs();
		Object actual = tree.transformRaw(inputs);
		assertEquals("detained", actual.toString());
	}

	public void testTransformingGary() {
		ISystem system = JBehaveSessionManager.makeEnginesFor(ICanCrossTheBorder.class);
		IEngineAsTree tree = system.tree("I assess the person");
		Node garyNode = tree.getRoot().match;
		assertEquals("Gary", ((BecauseForConstraint) garyNode.constraint.getBecause()).called);
		Object[] inputs = garyNode.constraint.getInputs();
		Object actual = tree.transformRaw(inputs);
		assertEquals("arrested", actual.toString());
	}

	public void testTransforming() {
		ISystem system = JBehaveSessionManager.makeEnginesFor(ICanCrossTheBorder.class);
		for (String systemName : system.engineNames()) {
			IEngineAsTree tree = system.tree(systemName);
			for (Node node : tree.allNodes()) {
				Object expected = node.constraint.getResult();
				Object actual = tree.transformRaw(node.constraint.getInputs());
				assertEquals(systemName + "." + getBecauseName(node), expected.toString(), actual.toString());
			}
		}
	}

	public void testICanCrossTheBorderWithGaryBroken() {
		Tests.assertThrowsWithMessage("The because was not true for the parameters. Offending method is public org.autoTdd.jbehave.because.IBecause2 org.autoTdd.schengen.AccessSteps.becauseThePersonIsCarryingDrugs() parameters are [Schengen [sarsState=false], Person [name=Gary but he hasn't got any drugs, money=10000, carryingDrugs=false]]", IllegalCaseException.class, new Runnable() {
			public void run() {
				JBehaveSessionManager.makeEnginesFor(ICanCrossTheBorderWithGaryBroken.class);
			}
		});
	}

	private String getBecauseName(Node node) {
		BecauseForConstraint becauseForConstraint = (BecauseForConstraint) node.constraint.getBecause();
		return becauseForConstraint.becauseAnnotationValue;

	}

}
