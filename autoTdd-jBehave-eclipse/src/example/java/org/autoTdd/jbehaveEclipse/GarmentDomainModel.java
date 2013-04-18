package org.autoTdd.jbehaveEclipse;

import org.autoTdd.jbehave.DSL;
import org.autoTdd.jbehave.ISpecifiesEngines;
import org.autoTdd.jbehave.junit.AutoTddJUnitReportingRunner;
import org.autoTdd.jbehave.junit.AutoTddJUnitStory;
import org.autoTdd.jbehaveEclipse.steps.GarmentSteps;
import org.junit.runner.RunWith;
import org.softwarefm.schengen.IEngineSteps;

@RunWith(AutoTddJUnitReportingRunner.class)
public class GarmentDomainModel extends AutoTddJUnitStory implements ISpecifiesEngines {

	@Override
	protected IEngineSteps[] engineSteps() {
		return new IEngineSteps[] { new GarmentSteps() };
	}

	public void specify() {
	}

}