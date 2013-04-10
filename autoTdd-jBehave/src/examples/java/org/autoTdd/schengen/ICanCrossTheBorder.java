package org.autoTdd.schengen;

import java.util.Arrays;
import java.util.Iterator;

import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.builder.internal.Node;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.jbehave.DSL;
import org.autoTdd.jbehave.IJBehaveAutoTddFactory;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.junit.runner.RunWith;
import org.softwarefm.utilities.clone.ICloner;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;

@RunWith(JUnitReportingRunner.class)
public class ICanCrossTheBorder extends JUnitStory {

	@Override
	public Configuration configuration() {
		return IJBehaveAutoTddFactory.Utils.normalConfiguration(getClass());
	}

	public InjectableStepsFactory stepsFactory() {
		return IJBehaveAutoTddFactory.Utils.makeStepsFactory(this, new AccessSteps(), new DetainSteps());
	}

	public static void main(String[] args) throws Throwable {
		IJBehaveAutoTddFactory factory = IJBehaveAutoTddFactory.Utils.autoTddFactory();
		IMutableSystemBuilder systemBuilder = factory.systemBuilderFor(ICloner.Utils.clonerWithUseClone(), ICanCrossTheBorder.class);
		systemBuilder.specify("I assess the person", DSL.specification(String.class, "Default", Schengen.class, Person.class));
		for (String name : systemBuilder.systemNames()) {
			IEngineAsTree tree = systemBuilder.builderFor(name).tree();

			System.out.println("Tree for " + name);
			System.out.println(tree);
			System.out.println("Applying for " + name);
			for (Iterator<Node> iterator = tree.allNodes().iterator(); iterator.hasNext();) {
				Node node = iterator.next();
				Object[] inputs = node.constraint.getInputs();
				Object actualResult = tree.transformRaw(inputs);
				System.out.println(Arrays.asList(inputs) + "-->" + actualResult);
			}
		}
	}

}
