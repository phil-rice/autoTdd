package org.autoTdd.jbehave.junit;

import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.ISystem;
import org.autoTdd.builder.internal.SystemBuilder;
import org.autoTdd.internal.AutoTddFactory;
import org.autoTdd.jbehave.IJBehaveAutoTddFactory;
import org.autoTdd.jbehave.ISpecifiesEngines;
import org.autoTdd.jbehave.IJBehaveAutoTddFactory.Utils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.softwarefm.schengen.IEngineSteps;
import org.softwarefm.utilities.clone.ICloner;

public abstract class AutoTddJUnitStory extends JUnitStory implements ISpecifiesEngines {

	private IMutableSystemBuilder systemBuilder;

	public AutoTddJUnitStory() {
	}

	abstract protected IEngineSteps[] engineSteps();

	abstract public void specify();

	@Override
	public Configuration configuration() {
		return IJBehaveAutoTddFactory.Utils.normalConfiguration(cloner(), getClass());
	}

	public ICloner cloner() {
		return ICloner.Utils.clonerWithUseClone();
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return IJBehaveAutoTddFactory.Utils.makeStepsFactory(this, engineSteps());
	}

	public IMutableSystemBuilder systemBuilder() {
		return systemBuilder;
	}

	@Override
	public void specify(IMutableSystemBuilder systemBuilder) {
		this.systemBuilder = systemBuilder;
	}
	
}