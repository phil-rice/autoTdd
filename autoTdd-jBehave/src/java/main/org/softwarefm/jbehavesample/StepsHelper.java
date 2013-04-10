package org.softwarefm.jbehavesample;

import java.lang.reflect.Method;
import java.util.List;

import org.autoTdd.jbehave.annotations.Because;
import org.autoTdd.jbehave.annotations.Called;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryManager;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCreator.ParameterisedStep;
import org.softwarefm.schengen.IEngineSteps;

public class StepsHelper {

	public static Story getStoryFor(ConfigurableEmbedder embeddable) {
		StoryManager storyManager = embeddable.configuredEmbedder().storyManager();
		Configuration configuration = embeddable.configuration();
		StoryPathResolver resolver = configuration.storyPathResolver();
		String storyPath = resolver.resolve(embeddable.getClass());
		Story story = storyManager.storyOfPath(storyPath);
		return story;
	}

	public static void visitMethodAnnotation(Step step, MethodAnnotationVisitor visitor) {
		Method method = findMethodFor(step);
		if (method != null) {
			Given given = method.getAnnotation(Given.class);
			Because because = method.getAnnotation(Because.class);
			When when = method.getAnnotation(When.class);
			Then then = method.getAnnotation(Then.class);
			Called called = method.getAnnotation(Called.class);
			if (given != null)
				visitor.visit(given);
			if (because != null)
				visitor.visit(because);
			if (when != null)
				visitor.visit(when);
			if (then != null)
				visitor.visit(then);
			if (called != null)
				visitor.visit(called);
		}

	}

	public static Method findMethodFor(Step step) {
		if (step instanceof ParameterisedStep) {
			ParameterisedStep parameterisedStep = (ParameterisedStep) step;
			return parameterisedStep.getMethod();
		}
		return null;
	}

	public static Object[] findParametersFor(Step step) {
		if (step instanceof ParameterisedStep) {
			ParameterisedStep parameterisedStep = (ParameterisedStep) step;
			Object[] convertedParameters = parameterisedStep.getConvertedParameters();
			return convertedParameters;
		}
		return new Object[0];
	}

	@SuppressWarnings("unchecked")
	public static  IEngineSteps findEngineStepsFor(Step step) {
		if (step instanceof ParameterisedStep) {
			ParameterisedStep parameterisedStep = (ParameterisedStep) step;
			Object stepsInstance = parameterisedStep.stepsInstance();
			if (stepsInstance instanceof IEngineSteps) {
				return (IEngineSteps) stepsInstance;
			}
		}
		return null;
	}

	public static List<CandidateSteps> getCandidateSteps(ConfigurableEmbedder embedder) {
		List<CandidateSteps> candidateSteps = embedder.stepsFactory().createCandidateSteps();
		return candidateSteps;
	}

	public static Object parameteriseAndRun(Step step) {
		try {
			if (step instanceof ParameterisedStep) {
				ParameterisedStep parameterisedStep = (ParameterisedStep) step;
				parameterisedStep.parametriseStep();
				if (!parameterisedStep.dryRun()) {
					Method method = findMethodFor(step);
					return method.invoke(parameterisedStep.stepsInstance(), parameterisedStep.getConvertedParameters());
				}
			} else
				step.perform(new UUIDExceptionWrapper());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;

	}

}
