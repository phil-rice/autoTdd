package org.autoTdd.jbehaveEclipse;

import org.autoTdd.jbehave.IJBehaveAutoTddFactory;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.softwarefm.schengen.IEngineSteps;

public abstract class AbstractStory extends JUnitStory {
	abstract protected IEngineSteps[] engineSteps(); 
	
	@Override
	public Configuration configuration() {
		return IJBehaveAutoTddFactory.Utils.normalConfiguration(getClass());
	}

	public InjectableStepsFactory stepsFactory() {
		return IJBehaveAutoTddFactory.Utils.makeStepsFactory(this, engineSteps());
	}

}
