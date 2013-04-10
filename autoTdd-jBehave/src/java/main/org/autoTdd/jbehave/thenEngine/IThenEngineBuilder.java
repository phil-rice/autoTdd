package org.autoTdd.jbehave.thenEngine;

public interface IThenEngineBuilder {

	IThenEngineBuilder register(Class<?> resultClass, IThenStrategy thenStrategy);

	IThenEngine build();
}
