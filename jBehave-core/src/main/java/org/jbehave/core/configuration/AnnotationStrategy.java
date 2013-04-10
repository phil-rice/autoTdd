package org.jbehave.core.configuration;

import java.lang.reflect.Method;

import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepType;

public interface AnnotationStrategy<A> {

	StepType stepType();

	int priority(A annotation);

	String value(A annotation);

	Object execute(Step step, Method method, Object object, Object[] parameters) throws Exception;

}
