package org.autoTdd.jbehave.junit;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.ISystem;
import org.autoTdd.builder.internal.Node;
import org.autoTdd.builder.internal.SystemBuilder;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.internal.AutoTddFactory;
import org.autoTdd.internal.Constraint;
import org.autoTdd.jbehave.ISpecifiesEngines;
import org.autoTdd.jbehave.JBehaveSessionManager;
import org.autoTdd.jbehave.internal.BecauseForConstraint;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.StoryRunner;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.NullStepMonitor;
import org.jbehave.core.steps.StepMonitor;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.softwarefm.utilities.collections.Files;

public class AutoTddJUnitReportingRunner extends Runner {
	private List<Description> storyDescriptions;
	private Embedder configuredEmbedder;
	private List<String> storyPaths;
	private Configuration configuration;
	private int numberOfTestCases;
	private Description rootDescription;
	List<CandidateSteps> candidateSteps;
	private ConfigurableEmbedder configurableEmbedder;
	private IMutableSystemBuilder systemBuilder;
	private Description verifyDescription;

	public AutoTddJUnitReportingRunner(Class<? extends ConfigurableEmbedder> testClass) throws Throwable {
		configurableEmbedder = testClass.newInstance();
		systemBuilder = new SystemBuilder(new AutoTddFactory());
		ISpecifiesEngines.Utils.specify(configurableEmbedder, systemBuilder);

		if (configurableEmbedder instanceof JUnitStories) {
			getStoryPathsFromJUnitStories(testClass);
		} else if (configurableEmbedder instanceof JUnitStory) {
			getStoryPathsFromJUnitStory();
		}
		configuration = configuredEmbedder.configuration();

		StepMonitor originalStepMonitor = createCandidateStepsWithNoMonitor();
		storyDescriptions = buildDescriptionFromStories();
		createCandidateStepsWith(originalStepMonitor);

		initRootDescription();
	}

	@Override
	public Description getDescription() {
		return rootDescription;
	}

	@Override
	public int testCount() {
		return numberOfTestCases;
	}

	@Override
	public void run(RunNotifier notifier) {
		AutoTddJUnitScenarioReporter junitReporter = new AutoTddJUnitScenarioReporter(notifier, numberOfTestCases, rootDescription);
		// tell the reporter how to handle pending steps
		junitReporter.usePendingStepStrategy(configuration.pendingStepStrategy());

		addToStoryReporterFormats(junitReporter);

		try {
			configuredEmbedder.runStoriesAsPaths(storyPaths);
			runAutoTddTests(junitReporter);
		} finally {
			configuredEmbedder.generateCrossReference();
		}
	}

	private void runAutoTddTests(AutoTddJUnitScenarioReporter junitReporter) {
		if (configurableEmbedder instanceof ISpecifiesEngines && verifyDescription != null) {
			File root = new File("target/autoTdd");
			ISpecifiesEngines specifiesEngines = (ISpecifiesEngines) configurableEmbedder;
			junitReporter.logger.info("Building system");
			junitReporter.notifier.fireTestStarted(verifyDescription);
			ISystem system = specifiesEngines.systemBuilder().build();
			for (String engineName : system.engineNames()) {
				File engineFile = new File(root, engineName +".atdd");
				root.mkdirs();
				Description engineDescription = findEngineDescription(engineName);
				if (engineDescription != null) {
					junitReporter.notifier.fireTestStarted(engineDescription);
					junitReporter.logger.info("Checking engine " + engineName);
					IEngineAsTree tree = system.tree(engineName);
					Files.setText(engineFile, tree.toString());
					System.out.println("Engine: " + engineName + "\n" + tree);
					for (Node node : tree.allNodes()) {
						Constraint constraint = node.constraint;
						Description constraintDescription = findConstrantDescription(engineDescription, (BecauseForConstraint) constraint.getBecause());
						junitReporter.notifier.fireTestStarted(constraintDescription);
						try {
							Object actual = tree.transformRaw(constraint.getInputs());
							Assert.assertEquals(constraint.getResult(), actual);
							junitReporter.notifier.fireTestFinished(constraintDescription);
							System.out.println("Checked [" + engineName + "] " + Arrays.asList(constraint.getInputs()) + " -- > " + constraint.getResult());
						} catch (Throwable e) {
							junitReporter.notifier.fireTestFailure(new Failure(constraintDescription, e));
						}
					}
					junitReporter.notifier.fireTestFinished(engineDescription);
				}
			}
			junitReporter.logger.info("Now running the verifications");
			junitReporter.notifier.fireTestFinished(verifyDescription);
		}
	}

	private Description findConstrantDescription(Description engineDescription, BecauseForConstraint constraint) {
		String situationString = constraint.situationString();
		for (Description description : engineDescription.getChildren())
			if (situationString.equals(description.getDisplayName()))
				return description;
		return null;
	}

	private Description findEngineDescription(String engineName) {
		for (Description description : verifyDescription.getChildren())
			if (engineName.equals(description.getDisplayName()))
				return description;
		return null;
	}

	public static EmbedderControls recommandedControls(Embedder embedder) {
		return embedder.embedderControls()
		// don't throw an exception on generating reports for failing stories
				.doIgnoreFailureInView(true)
				// don't throw an exception when a story failed
				.doIgnoreFailureInStories(true)
				// .doVerboseFailures(true)
				.useThreads(1);
	}

	private void createCandidateStepsWith(StepMonitor stepMonitor) {
		// reset step monitor and recreate candidate steps
		configuration.useStepMonitor(stepMonitor);
		getCandidateSteps();
		for (CandidateSteps step : candidateSteps) {
			step.configuration().useStepMonitor(stepMonitor);
		}
	}

	private StepMonitor createCandidateStepsWithNoMonitor() {
		StepMonitor usedStepMonitor = configuration.stepMonitor();
		createCandidateStepsWith(new NullStepMonitor());
		return usedStepMonitor;
	}

	private void getStoryPathsFromJUnitStory() {
		configuredEmbedder = configurableEmbedder.configuredEmbedder();
		StoryPathResolver resolver = configuredEmbedder.configuration().storyPathResolver();
		storyPaths = Arrays.asList(resolver.resolve(configurableEmbedder.getClass()));
	}

	@SuppressWarnings("unchecked")
	private void getStoryPathsFromJUnitStories(Class<? extends ConfigurableEmbedder> testClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		configuredEmbedder = configurableEmbedder.configuredEmbedder();
		Method method = makeStoryPathsMethodPublic(testClass);
		storyPaths = ((List<String>) method.invoke((JUnitStories) configurableEmbedder, (Object[]) null));
	}

	private Method makeStoryPathsMethodPublic(Class<? extends ConfigurableEmbedder> testClass) throws NoSuchMethodException {
		Method method;
		try {
			method = testClass.getDeclaredMethod("storyPaths", (Class[]) null);
		} catch (NoSuchMethodException e) {
			method = testClass.getMethod("storyPaths", (Class[]) null);
		}
		method.setAccessible(true);
		return method;
	}

	private void getCandidateSteps() {
		// candidateSteps = configurableEmbedder.configuredEmbedder()
		// .stepsFactory().createCandidateSteps();
		InjectableStepsFactory stepsFactory = configurableEmbedder.stepsFactory();
		if (stepsFactory != null) {
			candidateSteps = stepsFactory.createCandidateSteps();
		} else {
			Embedder embedder = configurableEmbedder.configuredEmbedder();
			candidateSteps = embedder.candidateSteps();
			if (candidateSteps == null || candidateSteps.isEmpty()) {
				candidateSteps = embedder.stepsFactory().createCandidateSteps();
			}
		}
	}

	private void initRootDescription() {
		rootDescription = Description.createSuiteDescription(configurableEmbedder.getClass());
		rootDescription.getChildren().addAll(storyDescriptions);
	}

	private void addToStoryReporterFormats(AutoTddJUnitScenarioReporter junitReporter) {
		StoryReporterBuilder storyReporterBuilder = configuration.storyReporterBuilder();
		StoryReporterBuilder.ProvidedFormat junitReportFormat = new StoryReporterBuilder.ProvidedFormat(junitReporter);
		storyReporterBuilder.withFormats(junitReportFormat);
	}

	@SuppressWarnings("unchecked")
	private List<Description> buildDescriptionFromStories() {
		AutoTddJUnitDescriptionGenerator descriptionGenerator = new AutoTddJUnitDescriptionGenerator(candidateSteps, configuration);
		StoryRunner storyRunner = new StoryRunner();
		List<Description> storyDescriptions = new ArrayList<Description>();

		addSuite(storyDescriptions, "BeforeStories");
		addStories(storyDescriptions, storyRunner, descriptionGenerator);
		addSuite(storyDescriptions, "AfterStories");
		int extraTests = 0;
		if (configurableEmbedder instanceof ISpecifiesEngines) {
			storyDescriptions.add(verifyDescription = Description.createSuiteDescription("Verifying AutoTddEngines"));
			try {
				ISystem system = JBehaveSessionManager.makeEnginesFor(configurableEmbedder.getClass());
				for (String engineName : system.engineNames()) {
					IEngineAsTree tree = system.tree(engineName);
					Description engineDescription = Description.createSuiteDescription(engineName);
					verifyDescription.addChild(engineDescription);
					for (Node node : tree.allNodes()) {
						extraTests++;
						BecauseForConstraint constraint = (BecauseForConstraint) node.constraint.getBecause();
						engineDescription.addChild(Description.createSuiteDescription(constraint.situationString()));
					}
				}
			} catch (Exception e) {
				throw new IllegalStateException("Cannot make engine for " + configurableEmbedder.getClass(), e);
			}
		}
		numberOfTestCases += descriptionGenerator.getTestCases() +  extraTests;

		return storyDescriptions;
	}

	private void addStories(List<Description> storyDescriptions, StoryRunner storyRunner, AutoTddJUnitDescriptionGenerator gen) {
		for (String storyPath : storyPaths) {
			Story parseStory = storyRunner.storyOfPath(configuration, storyPath);
			Description descr = gen.createDescriptionFrom(parseStory);
			storyDescriptions.add(descr);
		}
	}

	private Description addSuite(List<Description> storyDescriptions, String name) {
		Description description = Description.createTestDescription(Object.class, name);
		storyDescriptions.add(description);
		numberOfTestCases++;
		return description;
	}
}