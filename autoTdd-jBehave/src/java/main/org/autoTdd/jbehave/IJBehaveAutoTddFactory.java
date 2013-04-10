package org.autoTdd.jbehave;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;

import org.autoTdd.IAutoTddFactory;
import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.jbehave.annotations.Because;
import org.autoTdd.jbehave.annotations.Called;
import org.autoTdd.jbehave.internal.JBehaveAutoTddFactory;
import org.autoTdd.steps.AutoTddStepTypes;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.AnnotationProcessor;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.DefaultAnnotationStrategy;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepType;
import org.softwarefm.schengen.IEngineSteps;
import org.softwarefm.utilities.clone.ICloner;

public interface IJBehaveAutoTddFactory {

	IMutableSystemBuilder systemBuilderFor(ICloner cloner, Class<? extends ConfigurableEmbedder> storyClass);

	public static class Utils {
		public static IJBehaveAutoTddFactory autoTddFactory(IAutoTddFactory factory) {
			return new JBehaveAutoTddFactory(factory);
		}

		public static IJBehaveAutoTddFactory autoTddFactory() {
			return new JBehaveAutoTddFactory();
		}

		public static Configuration normalConfiguration(Class<? extends ConfigurableEmbedder> clazz) {
			Keywords keywords = IJBehaveAutoTddFactory.Utils.keywords();
			Configuration useStoryReporterBuilder = new MostUsefulConfiguration().useKeywords(keywords)//
					.useStoryParser(new RegexStoryParser(keywords)).useStoryLoader(new LoadFromClasspath(clazz));
			return useStoryReporterBuilder;
		}

		public static Keywords keywords() {
			Keywords keywords = new LocalizedKeywords(Locale.getDefault());
			keywords.addStartingWord(AutoTddStepTypes.BECAUSE, "Because");
			keywords.addStartingWord(AutoTddStepTypes.CALLED, "Called");
			return keywords;
		}

		public static InjectableStepsFactory makeStepsFactory(ConfigurableEmbedder embedder, IEngineSteps... engineSteps) {
			AnnotationProcessor processor = new AnnotationProcessor() {
				private Object lastThen;
				{
					register(Given.class, new DefaultAnnotationStrategy<Given>(StepType.GIVEN) {

						public int priority(Given annotation) {
							return annotation.priority();
						}

						public String value(Given annotation) {
							return annotation.value();
						}

						@Override
						public Object execute(Step step, Method method, Object object, Object[] parameters) throws Exception {
							lastThen = null;
							return super.execute(step, method, object, parameters);
						}
					});
					register(When.class, new DefaultAnnotationStrategy<When>(StepType.WHEN) {

						public int priority(When annotation) {
							return annotation.priority();
						}

						public String value(When annotation) {
							return annotation.value();
						}

					});
					register(Because.class, new DefaultAnnotationStrategy<Because>(AutoTddStepTypes.BECAUSE) {
						@Override
						public int priority(Because annotation) {
							return annotation.priority();
						}

						@Override
						public String value(Because annotation) {
							return annotation.value();
						}

						@Override
						public Object execute(Step step, Method method, Object object, Object[] parameters) throws Exception {
							Object result = super.execute(step, method, object, parameters);
							if (!(result instanceof IBecause)) {
								throw new IllegalStateException("Results from @Because methods must be of type IBecause. Offending method is " + method);
							}
							IBecause because = (IBecause) result;
							boolean actual = IBecause.Utils.execute(because, step);
							if (!actual)
								throw new IllegalStateException("The because was not true for the parameters. Offending method is " + method + " parameters are " + Arrays.asList(parameters));
							return result;
						}

					});
					register(Then.class, new DefaultAnnotationStrategy<Then>(StepType.THEN) {
						@Override
						public int priority(Then annotation) {
							return annotation.priority();
						}

						@Override
						public String value(Then annotation) {
							return annotation.value();
						}

						@Override
						public Object execute(Step step, Method method, Object object, Object[] parameters) throws Exception {
							Object result = super.execute(step, method, object, parameters);
							return result;
						}
					});
					register(Called.class, new DefaultAnnotationStrategy<Called>(AutoTddStepTypes.CALLED) {
						@Override
						public int priority(Called annotation) {
							return annotation.priority();
						}

						@Override
						public String value(Called annotation) {
							return annotation.value();
						}

					});
				}
			};
			return new InstanceStepsFactory(embedder.configuration(), processor, (Object[]) engineSteps);
		}
	}
}
