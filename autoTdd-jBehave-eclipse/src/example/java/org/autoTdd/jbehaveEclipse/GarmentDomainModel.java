package org.autoTdd.jbehaveEclipse;

import org.autoTdd.jbehaveEclipse.steps.GarmentSteps;
import org.junit.runner.RunWith;
import org.softwarefm.schengen.IEngineSteps;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;

@RunWith(JUnitReportingRunner.class)
public class GarmentDomainModel extends AbstractStory {

	@Override
	protected IEngineSteps[] engineSteps() {
		return new IEngineSteps[] { new GarmentSteps() };
	}

}