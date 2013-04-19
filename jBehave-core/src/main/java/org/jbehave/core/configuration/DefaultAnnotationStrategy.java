package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepType;

abstract public class DefaultAnnotationStrategy<A extends Annotation> implements AnnotationStrategy<A> {

	private StepType stepType;

	public DefaultAnnotationStrategy(StepType stepType) {
		this.stepType = stepType;
	}

	public StepType stepType() {
		return stepType;
	}

	public Object execute(Step step, Method method, A annotation, Object object, Object[] parameters) throws Exception {
		Object result = method.invoke(object, parameters);
		return result;
	}

}