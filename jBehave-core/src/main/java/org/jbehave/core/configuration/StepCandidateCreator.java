package org.jbehave.core.configuration;

import java.lang.reflect.Method;

import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepType;

public interface StepCandidateCreator {

	StepCandidate create(StepType stepType, String stepPatternAsString, int priority);

}
