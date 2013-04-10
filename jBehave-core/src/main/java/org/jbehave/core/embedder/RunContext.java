package org.jbehave.core.embedder;

import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryRunner.State;
import org.jbehave.core.model.GivenStory;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryExecutor;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * The context for running a story.
 */
public class RunContext {
	/**
	 * 
	 */
	private final StoryRunner storyRunner;
	private final Configuration configuration;
	private final List<CandidateSteps> candidateSteps;
	private final String path;
	final MetaFilter filter;
	final boolean givenStory;
	private State state;
	private RunContext parentContext;

	public RunContext(StoryRunner storyRunner, Configuration configuration, InjectableStepsFactory stepsFactory, String path, MetaFilter filter) {
		this(storyRunner, configuration, stepsFactory.createCandidateSteps(), path, filter);
	}

	public RunContext(StoryRunner storyRunner, Configuration configuration, List<CandidateSteps> steps, String path, MetaFilter filter) {
		this(storyRunner, configuration, steps, path, filter, false, null);
	}

	private RunContext(StoryRunner storyRunner, Configuration configuration, List<CandidateSteps> steps, String path, MetaFilter filter, boolean givenStory, RunContext parentContext) {
		this.storyRunner = storyRunner;
		this.configuration = configuration;
		this.candidateSteps = steps;
		this.path = path;
		this.filter = filter;
		this.givenStory = givenStory;
		this.parentContext = parentContext;
		resetState();
	}

	public StoryExecutor storyExecutor() {
		return configuration.storyExecutor();
	}

	public void interruptIfCancelled() throws InterruptedException {
		for (Story story : this.storyRunner.cancelledStories.keySet()) {
			if (path.equals(story.getPath())) {
				throw new InterruptedException(path);
			}
		}
	}

	public boolean dryRun() {
		return configuration.storyControls().dryRun();
	}

	public Configuration configuration() {
		return configuration;
	}

	public List<CandidateSteps> candidateSteps() {
		return candidateSteps;
	}

	public boolean givenStory() {
		return givenStory;
	}

	public String path() {
		return path;
	}

	public FilteredStory filter(Story story) {
		return new FilteredStory(filter, story, configuration.storyControls());
	}

	public String metaFilterAsString() {
		return filter.asString();
	}

	public List<Step> collectBeforeOrAfterStorySteps(Story story, Stage stage) {
		return configuration.stepCollector().collectBeforeOrAfterStorySteps(candidateSteps, story, stage, givenStory);
	}

	public List<Step> collectBeforeOrAfterScenarioSteps(Meta storyAndScenarioMeta, Stage stage, ScenarioType type) {
		return configuration.stepCollector().collectBeforeOrAfterScenarioSteps(candidateSteps, storyAndScenarioMeta, stage, type);
	}

	public List<Step> collectScenarioSteps(Scenario scenario, Map<String, String> parameters) {
		return configuration.stepCollector().collectScenarioSteps(candidateSteps, scenario, parameters);
	}

	public RunContext childContextFor(GivenStory givenStory) {
		String actualPath = configuration.pathCalculator().calculate(path, givenStory.getPath());
		return new RunContext(this.storyRunner, configuration, candidateSteps, actualPath, filter, true, this);
	}

	public State state() {
		return state;
	}

	public void stateIs(State state) {
		this.state = state;
		if (parentContext != null) {
			parentContext.stateIs(state);
		}
	}

	public boolean failureOccurred() {
		return this.storyRunner.failed(state);
	}

	public void resetState() {
		this.state = this.storyRunner.new FineSoFar();
	}

}