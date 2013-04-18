package org.autoTdd.jbehave;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.ISystem;
import org.autoTdd.builder.internal.SystemBuilder;
import org.autoTdd.internal.AutoTddFactory;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.softwarefm.jbehavesample.StepsHelper;
import org.softwarefm.utilities.exceptions.WrappedException;

/**
 * This is the result of unpalatable design decisions
 * 
 * <ul>
 * <li>Configuration should (I feel stateless)
 * <li>I want to minimise JDepend-core changes
 * <li>I need a mutable system builder per build (don't want to lock down to only one thread)
 * <li>So we now need a static variable...very embaressing
 * </ul>
 * So at least it's thread local meaning that we can do different things at different times
 * 
 * @author Phil
 * 
 */
public class JBehaveSessionManager {

	private static ThreadLocal<IMutableSystemBuilder> systemBuilders = new ThreadLocal<IMutableSystemBuilder>();

	public static SystemBuilder startBuild() {
		return startBuild(new AutoTddFactory());
	}

	public static SystemBuilder startBuild(AutoTddFactory factory) {
		SystemBuilder systemBuilder = new SystemBuilder(factory);
		systemBuilders.set(systemBuilder);
		return systemBuilder;
	}

	public static IMutableSystemBuilder systemBuilder() {
		IMutableSystemBuilder systemBuilder = systemBuilders.get();
		if (systemBuilder == null)
			throw new NullPointerException("You haven't initialised the system builder by using " + JBehaveSessionManager.class.getSimpleName() + ".startBuild");
		else
			return systemBuilder;
	}

	public static ISystem makeEnginesFor(ConfigurableEmbedder configurableEmbedder, Runnable runnable) {
		try {
			AutoTddFactory factory = new AutoTddFactory();
			SystemBuilder systemBuilder = startBuild(factory);
			initialise(configurableEmbedder);
			runnable.run();
			return systemBuilder.build();
		} catch (Throwable e) {
			throw WrappedException.wrap(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static ISystem makeEnginesFor(Class<? extends ConfigurableEmbedder>... classes) {
		try {
			AutoTddFactory factory = new AutoTddFactory();
			SystemBuilder systemBuilder = startBuild(factory);
			Map<Class<?>, ConfigurableEmbedder> instances = new HashMap<Class<?>, ConfigurableEmbedder>();
			for (Class<? extends ConfigurableEmbedder> clazz : classes) {
				ConfigurableEmbedder embedder = clazz.newInstance();
				instances.put(clazz, embedder);
				ISpecifiesEngines.Utils.specify(embedder, systemBuilder);
			}
			for (ConfigurableEmbedder embedder : instances.values()) {
				Story story = StepsHelper.getStoryFor(embedder);
				Configuration configuration = embedder.configuration();
				InjectableStepsFactory stepsFactory = embedder.stepsFactory();
				List<CandidateSteps> candidateSteps = stepsFactory.createCandidateSteps();
				embedder.configuredEmbedder().storyRunner().run(configuration, candidateSteps, story);
			}

			return systemBuilder.build();
		} catch (Throwable e) {
			throw WrappedException.wrap(e);
		}
	}

	public static void initialise(Object specifier) {
		if (specifier instanceof ISpecifiesEngines)
			((ISpecifiesEngines) specifier).specify(systemBuilder());
	}

	public static IMutableSystemBuilder createIfNeededSystemBuilder() {
		IMutableSystemBuilder systemBuilder = systemBuilders.get();
		if (systemBuilder == null)
			return startBuild(new AutoTddFactory());
		else
			return systemBuilder;
	}

}
