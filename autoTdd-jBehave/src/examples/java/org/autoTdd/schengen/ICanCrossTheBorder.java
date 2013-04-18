package org.autoTdd.schengen;

import org.autoTdd.jbehave.DSL;
import org.autoTdd.jbehave.junit.AutoTddJUnitReportingRunner;
import org.autoTdd.jbehave.junit.AutoTddJUnitStory;
import org.junit.runner.RunWith;
import org.softwarefm.schengen.IEngineSteps;

@RunWith(AutoTddJUnitReportingRunner.class)
public class ICanCrossTheBorder extends AutoTddJUnitStory {

	@Override
	protected IEngineSteps[] engineSteps() {
		return new IEngineSteps[] { new AccessSteps(), new DetainSteps() };
	}

	@Override
	public void specify() {
		systemBuilder().specify("I assess the person", DSL.specification(String.class, "Default", Schengen.class, Person.class));
	}

}
