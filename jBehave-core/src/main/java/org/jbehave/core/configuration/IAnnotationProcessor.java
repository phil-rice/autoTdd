package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepType;

public interface IAnnotationProcessor {

	<A extends Annotation> void register(Class<A> annotationClass, AnnotationStrategy<A> strategy);

	void execute(Step step, Method method, Object object, Object[] parameters) throws Exception;

	void addCandidates(List<StepCandidate> candidates, Method method, StepCandidateCreator creator);

	String typeWord(StepType stepType);

	<T> List<T> mapRegistered(IAnnotationMapper<T> mapper);
}