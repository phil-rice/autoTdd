package org.autoTdd.jbehave.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.autoTdd.IAutoTddFactory;
import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.ISystem;
import org.autoTdd.builder.internal.SystemBuilder;
import org.autoTdd.jbehave.IJBehaveAutoTddFactory;
import org.autoTdd.jbehave.ISpecifiesEngines;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.softwarefm.jbehavesample.StepsHelper;
import org.softwarefm.utilities.clone.ICloner;
import org.softwarefm.utilities.exceptions.WrappedException;

public class JBehaveAutoTddFactory implements IJBehaveAutoTddFactory {

	private IAutoTddFactory factory;
	private ICloner cloner;

	public JBehaveAutoTddFactory(IAutoTddFactory factory, ICloner cloner) {
		this.factory = factory;
		this.cloner = cloner;
	}

	@Override
	public ISystem makeEnginesFor(Class<? extends ConfigurableEmbedder>... classes) {
		try {
			IMutableSystemBuilder systemBuilder = new SystemBuilder(factory);
			Map<Class<?>, ConfigurableEmbedder> instances = new HashMap<Class<?>, ConfigurableEmbedder>();
			for (Class<? extends ConfigurableEmbedder> clazz : classes) {
				ConfigurableEmbedder embedder = clazz.newInstance();
				instances.put(clazz, embedder);
				if (embedder instanceof ISpecifiesEngines)
					((ISpecifiesEngines) embedder).specify(systemBuilder);
			}
			for (ConfigurableEmbedder embedder: instances.values()){
				Story story = StepsHelper.getStoryFor(embedder);
				Configuration configuration = embedder.configuration();
				InjectableStepsFactory stepsFactory = embedder.stepsFactory();
				List<CandidateSteps> candidateSteps = stepsFactory.createCandidateSteps();
				embedder.configuredEmbedder().storyRunner().run(configuration, candidateSteps, story);
			}
				
			return systemBuilder;
		} catch (Throwable e) {
			throw WrappedException.wrap(e);
		}
	}
}
