package org.jbehave.core.reporters;

import org.jbehave.core.embedder.RunContext;
import org.jbehave.core.embedder.StoryRunner.State;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector.Stage;

public interface StoryExecutor {


	State run(State state, Step step);


}
