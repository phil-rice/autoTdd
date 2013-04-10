package org.jbehave.core.configuration;

import org.jbehave.core.embedder.StoryRunner.State;
import org.jbehave.core.reporters.StoryExecutor;
import org.jbehave.core.steps.Step;

public class DefaultStoryExecutor implements StoryExecutor {

	public State run(State state, Step step) {
		return state.run(step);
	}

}
