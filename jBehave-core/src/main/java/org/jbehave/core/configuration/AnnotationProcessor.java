package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.steps.PatternVariantBuilder;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCandidate;
import org.jbehave.core.steps.StepType;
import org.jbehave.core.steps.Steps.DuplicateCandidateFound;

public class AnnotationProcessor implements IAnnotationProcessor {
	public static AnnotationProcessor defaultAnnotationProcessor() {
		AnnotationProcessor processor = new AnnotationProcessor();
		processor.register(Given.class, new DefaultAnnotationStrategy<Given>(StepType.GIVEN) {

			public int priority(Given annotation) {
				return annotation.priority();
			}

			public String value(Given annotation) {
				return annotation.value();
			}

			public String typeWord() {
				return "Given";
			}
		});
		processor.register(Then.class, new DefaultAnnotationStrategy<Then>(StepType.THEN) {
			public int priority(Then annotation) {
				return annotation.priority();
			}

			public String value(Then annotation) {
				return annotation.value();
			}

			public String typeWord() {
				return "Then";
			}

		});
		processor.register(When.class, new DefaultAnnotationStrategy<When>(StepType.WHEN) {

			public int priority(When annotation) {
				return annotation.priority();
			}

			public String value(When annotation) {
				return annotation.value();
			}

			public String typeWord() {
				return "When";
			}

		});
		return processor;
	}

	private final Map<Class<? extends Annotation>, AnnotationStrategy<?>> annotationClassToStrategy = new HashMap<Class<? extends Annotation>, AnnotationStrategy<?>>();
	private final Map<StepType, AnnotationStrategy<?>> stepTypeToStrategy = new HashMap<StepType, AnnotationStrategy<?>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jbehave.core.configuration.IAnnotationProcessor#register(java.lang.Class, org.jbehave.core.configuration.AnnotationStrategy)
	 */
	public <A extends Annotation> void register(Class<A> annotationClass, AnnotationStrategy<A> strategy) {
		annotationClassToStrategy.put(annotationClass, strategy);
		stepTypeToStrategy.put(strategy.stepType(), strategy);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addCandidates(List<StepCandidate> candidates, Method method, StepCandidateCreator creator) {
		for (Entry<Class<? extends Annotation>, AnnotationStrategy<?>> entry : annotationClassToStrategy.entrySet()) {
			process(candidates, method, (Class) entry.getKey(), (AnnotationStrategy) entry.getValue(), creator);
		}
	}

	private <T extends Annotation> void process(List<StepCandidate> candidates, Method method, Class<T> annotationClass, AnnotationStrategy<T> annotationStrategy, StepCandidateCreator creator) {
		T annotation = method.getAnnotation(annotationClass);
		if (annotation != null) {
			int priority = annotationStrategy.priority(annotation);
			String value = annotationStrategy.value(annotation);
			StepType stepType = annotationStrategy.stepType();
			addCandidatesFromVariants(candidates, method, stepType, value, priority, creator);
			addCandidatesFromAliases(candidates, method, stepType, priority, creator);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jbehave.core.configuration.IAnnotationProcessor#execute(org.jbehave.core.steps.Step, java.lang.reflect.Method, java.lang.Object, java.lang.Object[])
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void execute(Step step, Method method, Object object, Object[] parameters) throws Exception {
		for (Entry<Class<? extends Annotation>, AnnotationStrategy<?>> entry : annotationClassToStrategy.entrySet()) {
			Annotation annotation = method.getAnnotation(entry.getKey());
			if (annotation != null)
				((AnnotationStrategy) entry.getValue()).execute(step, method, annotation, object, parameters);
		}

	}

	private void addCandidatesFromVariants(List<StepCandidate> candidates, Method method, StepType stepType, String value, int priority, StepCandidateCreator creator) {
		PatternVariantBuilder b = new PatternVariantBuilder(value);
		for (String variant : b.allVariants()) {
			addCandidate(candidates, method, stepType, variant, priority, creator);
		}
	}

	private void addCandidatesFromAliases(List<StepCandidate> candidates, Method method, StepType stepType, int priority, StepCandidateCreator creator) {
		if (method.isAnnotationPresent(Aliases.class)) {
			String[] aliases = method.getAnnotation(Aliases.class).values();
			for (String alias : aliases) {
				addCandidatesFromVariants(candidates, method, stepType, alias, priority, creator);
			}
		}
		if (method.isAnnotationPresent(Alias.class)) {
			String alias = method.getAnnotation(Alias.class).value();
			addCandidatesFromVariants(candidates, method, stepType, alias, priority, creator);
		}
	}

	private void addCandidate(List<StepCandidate> candidates, Method method, StepType stepType, String stepPatternAsString, int priority, StepCandidateCreator creator) {
		checkForDuplicateCandidates(candidates, stepType, stepPatternAsString);
		StepCandidate candidate = creator.create(stepType, stepPatternAsString, priority);
		candidates.add(candidate);
	}

	private void checkForDuplicateCandidates(List<StepCandidate> candidates, StepType stepType, String patternAsString) {
		for (StepCandidate candidate : candidates) {
			if (candidate.getStepType() == stepType && candidate.getPatternAsString().equals(patternAsString)) {
				throw new DuplicateCandidateFound(stepType, patternAsString);
			}
		}
	}

	public String typeWord(StepType stepType) {
		AnnotationStrategy<?> annotationStrategy = stepTypeToStrategy.get(stepType);
		if (annotationStrategy == null)
			throw new IllegalArgumentException("Step type: " + stepType + " not registered. Legal values are " + stepTypeToStrategy.keySet());
		return annotationStrategy.typeWord();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <T> List<T> mapRegistered(IAnnotationMapper<T> mapper) {
		List<T> result = new ArrayList<T>();
		for (Entry<Class<? extends Annotation>, AnnotationStrategy<?>> entry : annotationClassToStrategy.entrySet())
			result.add((T) mapper.transform((Class) entry.getKey(), (AnnotationStrategy) entry.getValue()));
		return result;
	}
}
