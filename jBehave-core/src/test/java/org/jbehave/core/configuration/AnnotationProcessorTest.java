package org.jbehave.core.configuration;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.jbehave.core.steps.StepType;
import org.junit.Test;

public class AnnotationProcessorTest extends TestCase {

	private AnnotationProcessor annotationProcessor;

	public void testTypeWordForDefault() {
		assertEquals("Given", annotationProcessor.typeWord(StepType.GIVEN));
		assertEquals("When", annotationProcessor.typeWord(StepType.WHEN));
		assertEquals("Then", annotationProcessor.typeWord(StepType.THEN));
	}

	public void testMapRegistered() {
		Set<String> expected = new HashSet<String>(Arrays.asList("<When>", "<Given>", "<Then>"));
		List<String> actual = annotationProcessor.mapRegistered(new IAnnotationMapper<String>() {
			public <A extends Annotation> String transform(Class<A> clazz, AnnotationStrategy<A> strategy) {
				assertEquals(clazz.getSimpleName(), strategy.typeWord());
				return "<" + strategy.typeWord() + ">";
			}
		});
		assertEquals(expected, new HashSet<String>(actual));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		annotationProcessor = AnnotationProcessor.defaultAnnotationProcessor();
	}

}
