package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.jbehave.core.annotations.AfterScenario.Outcome.ANY;
import static org.jbehave.core.annotations.AfterScenario.Outcome.FAILURE;
import static org.jbehave.core.annotations.AfterScenario.Outcome.SUCCESS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.AnnotationProcessor;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.IAnnotationProcessor;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.configuration.StepCandidateCreator;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * <p>
 * Default implementation of {@link CandidateSteps} which provides the step candidates that match the steps being run.
 * </p>
 * <p>
 * To provide your step candidate methods, you can:
 * <ul>
 * <li>pass in the steps instance type and the steps factory used to instantiate the instance if any candidate steps are matched (lazy "has-a" relationship)</li>
 * <li>pass in the steps instance, instantiated regardless of whether the candidate steps are matched (eager "has-a" relationship)</li>
 * <li>extend the {@link Steps} class, in which case the instance is the extended {@link Steps} class itself ("is-a" relationship)</li>
 * </ul>
 * <b>The "has-a" design model, in which the steps instance is passed in, is strongly recommended over the "is-a" model as it does not have tie-ins in the {@link Steps} class implementation</b>.
 * </p>
 * <p>
 * You can define the methods that should be run by annotating them with {@link Given @Given}, {@link When @When} or {@link Then @Then}, and providing as a value for each annotation a pattern matches the textual step. The value is interpreted by the {@link StepPatternParser}, which by default is a {@link RegexPrefixCapturingPatternParser} that interprets the words starting with '$' as parameters.
 * </p>
 * <p>
 * For instance, you could define a method as:
 * 
 * <pre>
 * @When("I log in as $username with password: $password")
 * public void logIn(String username, String password) { //... }
 * </pre>
 * 
 * and this would match the step:
 * 
 * <pre>
 * When I log in as Liz with password: Pa55word
 * </pre>
 * 
 * </p>
 * <p>
 * When the step is performed, the parameters matched will be passed to the method, so in this case the effect will be to invoke:
 * </p>
 * 
 * <pre>
 * logIn(&quot;Liz&quot;, &quot;Pa55word&quot;);
 * </pre>
 * <p>
 * The {@link Configuration} can be used to provide customize the {@link StepCandidate}s that are created, e.g. providing a step monitor or creating them in "dry run" mode.
 * </p>
 */
public class Steps implements CandidateSteps {

	private final Configuration configuration;
	private Class<?> type;
	private InjectableStepsFactory stepsFactory;
	private final IAnnotationProcessor annotationProcessor;

	/**
	 * Creates Steps with default configuration for a class extending this instance and containing the candidate step methods
	 */
	public Steps() {
		this(new MostUsefulConfiguration());
	}

	/**
	 * Creates Steps with given custom configuration for a class extending this instance and containing the candidate step methods
	 * 
	 * @param configuration
	 *            the Configuration
	 */
	public Steps(Configuration configuration) {
		this.configuration = configuration;
		this.type = this.getClass();
		this.stepsFactory = new InstanceStepsFactory(configuration,this);
		this.annotationProcessor = AnnotationProcessor.defaultAnnotationProcessor();
	}

	/**
	 * Creates Steps with given custom configuration and a steps instance containing the candidate step methods
	 * 
	 * @param configuration
	 *            the Configuration
	 * @param instance
	 *            the steps instance
	 */
	public Steps(Configuration configuration, Object instance) {
		this.configuration = configuration;
		this.type = instance.getClass();
		this.stepsFactory = new InstanceStepsFactory(configuration,instance);
		this.annotationProcessor = AnnotationProcessor.defaultAnnotationProcessor();
	}

	/**
	 * Creates Steps with given custom configuration and a steps instance type containing the candidate step methods. The steps instance is created using the steps instance factory provided.
	 * 
	 * @param configuration
	 *            the Configuration
	 * @param type
	 *            the steps instance type
	 * @param stepsFactory
	 *            the {@link InjectableStepsFactory}
	 */
	public Steps(Configuration configuration, Class<?> type, InjectableStepsFactory stepsFactory, IAnnotationProcessor annotationProcessor) {
		this.configuration = configuration;
		this.type = type;
		this.stepsFactory = stepsFactory;
		this.annotationProcessor = annotationProcessor;
	}

	public Class<?> type() {
		return type;
	}

	public Object instance() {
		return stepsFactory.createInstanceOfType(type);
	}

	public Configuration configuration() {
		return configuration;
	}

	public List<StepCandidate> listCandidates() {
		List<StepCandidate> candidates = new ArrayList<StepCandidate>();
		for (final Method method : allMethods())
			annotationProcessor.addCandidates(candidates, method, new StepCandidateCreator() {
				public StepCandidate create(StepType stepType, String stepPatternAsString, int priority) {
					StepCandidate candidate = new StepCandidate(stepPatternAsString, priority, stepType, method, type, stepsFactory, configuration.keywords(), configuration.stepPatternParser(), configuration.parameterConverters(), configuration.parameterControls(), annotationProcessor);
					candidate.useStepMonitor(configuration.stepMonitor());
					candidate.useParanamer(configuration.paranamer());
					candidate.doDryRun(configuration.storyControls().dryRun());
					if (method.isAnnotationPresent(Composite.class)) {
						candidate.composedOf(method.getAnnotation(Composite.class).steps());
					}
					return candidate;
				}
			});
		return candidates;
	}

	public List<BeforeOrAfterStep> listBeforeOrAfterStories() {
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		steps.addAll(stepsHaving(Stage.BEFORE, BeforeStories.class));
		steps.addAll(stepsHaving(Stage.AFTER, AfterStories.class));
		return steps;
	}

	public List<BeforeOrAfterStep> listBeforeOrAfterStory(boolean givenStory) {
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		steps.addAll(stepsHaving(Stage.BEFORE, BeforeStory.class, givenStory));
		steps.addAll(stepsHaving(Stage.AFTER, AfterStory.class, givenStory));
		return steps;
	}

	public List<BeforeOrAfterStep> listBeforeOrAfterScenario(ScenarioType type) {
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		steps.addAll(scenarioStepsHaving(type, Stage.BEFORE, BeforeScenario.class));
		steps.addAll(scenarioStepsHaving(type, Stage.AFTER, AfterScenario.class, ANY, SUCCESS, FAILURE));
		return steps;
	}

	private boolean runnableStoryStep(Annotation annotation, boolean givenStory) {
		boolean uponGivenStory = uponGivenStory(annotation);
		return uponGivenStory == givenStory;
	}

	private boolean uponGivenStory(Annotation annotation) {
		if (annotation instanceof BeforeStory) {
			return ((BeforeStory) annotation).uponGivenStory();
		} else if (annotation instanceof AfterStory) {
			return ((AfterStory) annotation).uponGivenStory();
		}
		return false;
	}

	private List<BeforeOrAfterStep> stepsHaving(Stage stage, Class<? extends Annotation> annotationClass) {
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		for (Method method : methodsAnnotatedWith(annotationClass)) {
			steps.add(createBeforeOrAfterStep(stage, method));
		}
		return steps;
	}

	private List<BeforeOrAfterStep> stepsHaving(Stage stage, Class<? extends Annotation> annotationClass, boolean givenStory) {
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		for (final Method method : methodsAnnotatedWith(annotationClass)) {
			if (runnableStoryStep(method.getAnnotation(annotationClass), givenStory)) {
				steps.add(createBeforeOrAfterStep(stage, method));
			}
		}
		return steps;
	}

	private List<BeforeOrAfterStep> scenarioStepsHaving(ScenarioType type, Stage stage, Class<? extends Annotation> annotationClass, Outcome... outcomes) {
		List<BeforeOrAfterStep> steps = new ArrayList<BeforeOrAfterStep>();
		for (Method method : methodsAnnotatedWith(annotationClass)) {
			ScenarioType scenarioType = scenarioType(method, annotationClass);
			if (type == scenarioType) {
				if (stage == Stage.BEFORE) {
					steps.add(createBeforeOrAfterStep(stage, method));
				}
				if (stage == Stage.AFTER) {
					Outcome scenarioOutcome = scenarioOutcome(method, annotationClass);
					for (Outcome outcome : outcomes) {
						if (outcome.equals(scenarioOutcome)) {
							steps.add(createBeforeOrAfterStep(stage, method, outcome));
						}
					}
				}
			}
		}
		return steps;
	}

	private ScenarioType scenarioType(Method method, Class<? extends Annotation> annotationClass) {
		if (annotationClass.isAssignableFrom(BeforeScenario.class)) {
			return ((BeforeScenario) method.getAnnotation(annotationClass)).uponType();
		}
		if (annotationClass.isAssignableFrom(AfterScenario.class)) {
			return ((AfterScenario) method.getAnnotation(annotationClass)).uponType();
		}
		return ScenarioType.NORMAL;
	}

	private Outcome scenarioOutcome(Method method, Class<? extends Annotation> annotationClass) {
		if (annotationClass.isAssignableFrom(AfterScenario.class)) {
			return ((AfterScenario) method.getAnnotation(annotationClass)).uponOutcome();
		}
		return Outcome.ANY;
	}

	private BeforeOrAfterStep createBeforeOrAfterStep(Stage stage, Method method) {
		return createBeforeOrAfterStep(stage, method, Outcome.ANY);
	}

	private BeforeOrAfterStep createBeforeOrAfterStep(Stage stage, Method method, Outcome outcome) {
		return new BeforeOrAfterStep(stage, method, outcome, new StepCreator(type, stepsFactory, configuration.parameterConverters(), configuration.parameterControls(), null, configuration.stepMonitor()));
	}

	private List<Method> allMethods() {
		return asList(type.getMethods());
	}

	private List<Method> methodsAnnotatedWith(Class<? extends Annotation> annotationClass) {
		List<Method> annotated = new ArrayList<Method>();
		for (Method method : allMethods()) {
			if (method.isAnnotationPresent(annotationClass)) {
				annotated.add(method);
			}
		}
		return annotated;
	}

	@SuppressWarnings("serial")
	public static class DuplicateCandidateFound extends RuntimeException {

		public DuplicateCandidateFound(StepType stepType, String patternAsString) {
			super(stepType + " " + patternAsString);
		}

	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(instance()).toString();
	}

}