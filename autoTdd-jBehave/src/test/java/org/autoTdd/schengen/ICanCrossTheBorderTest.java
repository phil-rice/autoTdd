package org.autoTdd.schengen;

import junit.framework.TestCase;

import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.builder.IEngineBuilder;
import org.autoTdd.builder.internal.Node;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.jbehave.BecauseForConstraint;
import org.autoTdd.jbehave.DSL;
import org.autoTdd.jbehave.IJBehaveAutoTddFactory;
import org.softwarefm.utilities.clone.ICloner;
import org.softwarefm.utilities.tests.Tests;

public class ICanCrossTheBorderTest extends TestCase {

	private IMutableSystemBuilder systemBuilder;

	public void testSystemNames(){
		Tests.assertListEquals(systemBuilder.systemNames(), "I assess the person");
	}
	
	
	public void testBuilds() {
		IEngineBuilder builder = systemBuilder.builderFor("I assess the person");
		IEngineAsTree tree = builder.tree();
		assertEquals(2, tree.allNodes().size());
		Node deborahNode = tree.getRoot();
		assertEquals("I said so", getBecauseName(deborahNode));
		Node franciscoNode = deborahNode.match;
		assertEquals("the person has a lot of money", getBecauseName(franciscoNode));
	}
	
	public void testTransformingDeborah(){
		Node deborahNode = systemBuilder.builderFor("I assess the person").tree().getRoot();
		Object actual = systemBuilder.builderFor("I assess the person").tree().transformRaw(deborahNode.constraint.getInputs());
		assertEquals( "the person should be $schengenResult[allowed]", actual.toString());
	}
	public void testTransformingFrancisco(){
		IEngineAsTree tree = systemBuilder.builderFor("I assess the person").tree();
		Node franciscoNode = tree.getRoot().match;
		Object[] inputs = franciscoNode.constraint.getInputs();
		Object actual = tree.transformRaw(inputs);
		assertEquals( "the person should be $schengenResult[detained]", actual.toString());
	}
	
	public void testTransforming(){
		for (String systemName: systemBuilder.systemNames()){
			IEngineAsTree tree =systemBuilder.builderFor(systemName).tree();
			for (Node node: tree.allNodes()){
				Object expected = node.constraint.getResult();
				Object actual = tree.transformRaw(node.constraint.getInputs());
				assertEquals(systemName + "." + getBecauseName(node), expected.toString(), actual.toString());
			}
		}
	}

	private String getBecauseName(Node node){
		BecauseForConstraint becauseForConstraint = (BecauseForConstraint) node.constraint.getBecause();
		return becauseForConstraint.becauseAnnotation.value();
		
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		systemBuilder = IJBehaveAutoTddFactory.Utils.autoTddFactory().systemBuilderFor(ICloner.Utils.clonerWithUseClone(), ICanCrossTheBorder.class);
		systemBuilder.specify("I assess the person", DSL.specification(String.class, "Default", Schengen.class, Person.class));
	}
}
