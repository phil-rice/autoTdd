package org.autoTdd.jbehave;

import java.util.Arrays;
import java.util.List;

import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.jbehave.annotations.AutoTddAnnotationProcessor;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.IAnnotationProcessor;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.softwarefm.utilities.clone.ICloner;

public class AutoTddInjectableStepsFactory extends InstanceStepsFactory {

	public IMutableSystemBuilder systemBuilder;
	private ICloner cloner;

	public AutoTddInjectableStepsFactory(Configuration configuration, ICloner cloner, IMutableSystemBuilder systemBuilder, List<Object> stepsInstances) {
		super(configuration, stepsInstances);
		this.cloner = cloner;
		this.systemBuilder = systemBuilder;
		if (systemBuilder == null)
			throw new NullPointerException();
	}

	public AutoTddInjectableStepsFactory(Configuration configuration, ICloner cloner, IMutableSystemBuilder systemBuilder, Object... stepsInstances) {
		this(configuration, cloner, systemBuilder, Arrays.asList(stepsInstances));
	}

	@Override
	protected IAnnotationProcessor createAnnotationProcessor() {
		return new AutoTddAnnotationProcessor(systemBuilder, cloner);
	}

}
