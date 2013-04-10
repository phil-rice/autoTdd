package org.autoTdd.jbehave.thenEngine.internal;

import org.autoTdd.jbehave.thenEngine.IThenEngine;
import org.autoTdd.jbehave.thenEngine.IThenEngineBuilder;
import org.autoTdd.jbehave.thenEngine.IThenStrategy;
import org.softwarefm.utilities.maps.ClassMap;

public class ThenEngineBuilder implements IThenEngineBuilder {

	private ClassMap<IThenStrategy> map = new ClassMap<IThenStrategy>();

	@Override
	public IThenEngineBuilder register(Class<?> resultClass, IThenStrategy thenStrategy) {
		map.put(resultClass, thenStrategy);
		return this;
	}

	@Override
	public IThenEngine build() {
		return new ThenEngine(map);
	}

}
