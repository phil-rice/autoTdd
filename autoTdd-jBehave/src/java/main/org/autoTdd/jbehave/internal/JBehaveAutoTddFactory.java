package org.autoTdd.jbehave.internal;

import org.autoTdd.IAutoTddFactory;
import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.internal.AutoTddFactory;
import org.autoTdd.jbehave.BuildAutoTddStoryExecutor;
import org.autoTdd.jbehave.IJBehaveAutoTddFactory;
import org.jbehave.core.ConfigurableEmbedder;
import org.softwarefm.utilities.clone.ICloner;

public class JBehaveAutoTddFactory implements IJBehaveAutoTddFactory {

	private IAutoTddFactory factory;

	public JBehaveAutoTddFactory() {
		this(new AutoTddFactory());
	}
	public JBehaveAutoTddFactory(IAutoTddFactory factory) {
		this.factory = factory;
	}

	@Override
	public IMutableSystemBuilder systemBuilderFor(ICloner cloner, Class<? extends ConfigurableEmbedder> storyClass) {
		return new BuildAutoTddStoryExecutor(factory, cloner, storyClass);
	}

}
