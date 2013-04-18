package org.autoTdd.jbehave;

import java.util.Locale;

import org.autoTdd.IAutoTddFactory;
import org.autoTdd.ISystem;
import org.autoTdd.internal.AutoTddFactory;
import org.autoTdd.jbehave.internal.JBehaveAutoTddFactory;
import org.autoTdd.steps.AutoTddStepTypes;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.softwarefm.schengen.IEngineSteps;
import org.softwarefm.utilities.clone.ICloner;

public interface IJBehaveAutoTddFactory {

	@SuppressWarnings("unchecked")
	ISystem makeEnginesFor(Class<? extends ConfigurableEmbedder>... classes);

	public static class Utils {
		public static IJBehaveAutoTddFactory autoTddFactory(IAutoTddFactory factory, ICloner cloner) {
			return new JBehaveAutoTddFactory(factory, cloner);
		}

		public static IJBehaveAutoTddFactory autoTddFactory(ICloner cloner) {
			return new JBehaveAutoTddFactory(new AutoTddFactory(), cloner);
		}

		public static Configuration normalConfiguration(ICloner cloner, Class<? extends ConfigurableEmbedder> clazz) {
			Keywords keywords = IJBehaveAutoTddFactory.Utils.keywords();
			Configuration configuration = new MostUsefulConfiguration().//
					useKeywords(keywords)//
					.useStoryParser(new RegexStoryParser(keywords)).//
					useStoryLoader(new LoadFromClasspath(clazz));
			return configuration;
		}

		public static Keywords keywords() {
			Keywords keywords = new LocalizedKeywords(Locale.getDefault());
			keywords.addStartingWord(AutoTddStepTypes.BECAUSE, "Because");
			keywords.addStartingWord(AutoTddStepTypes.CALLED, "Called");
			return keywords;
		}

		public static InjectableStepsFactory makeStepsFactory(ConfigurableEmbedder embedder, IEngineSteps... engineSteps) {
			if (embedder instanceof ISpecifiesEngines) {
				ISpecifiesEngines specifiesEngines = (ISpecifiesEngines) embedder;
				return new AutoTddInjectableStepsFactory(embedder.configuration(), specifiesEngines.cloner(), specifiesEngines.systemBuilder(), (Object[]) engineSteps);
			} else
				return new InstanceStepsFactory(embedder.configuration(), (Object[]) engineSteps);
		}
	}

}
