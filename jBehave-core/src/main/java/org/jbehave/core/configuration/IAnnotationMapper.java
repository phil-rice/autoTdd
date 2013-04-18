package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;

public interface IAnnotationMapper<T> {

	<A extends Annotation> T transform(Class<A> clazz, AnnotationStrategy<A> strategy);
}
