package org.autoTdd.jbehave.thenEngine.internal;

import java.lang.reflect.Method;

import org.autoTdd.jbehave.thenEngine.IThenEngine;
import org.autoTdd.jbehave.thenEngine.IThenStrategy;
import org.jbehave.core.annotations.Then;
import org.softwarefm.jbehavesample.exceptions.CannotExecuteThenException;
import org.softwarefm.utilities.maps.ClassMap;

public class ThenEngine implements IThenEngine {

	private ClassMap<IThenStrategy> map;

	public ThenEngine(ClassMap<IThenStrategy> map) {
		this.map = map;
	}

	@Override
	public void execute(Then then, Method method, Object result) {
		Class<? extends Object> clazz = result == null ? Void.class : result.getClass();
		IThenStrategy thenStrategy = map.get(clazz);
		if (thenStrategy == null)
			throw new CannotExecuteThenException(then, method, result);
		thenStrategy.execute(then, method, result);

	}

}
