package org.autoTdd.engine.internal;

import org.autoTdd.engine.IEngine;
import org.autoTdd.engine.IEngine1;

public class Engine1<Result, Input> implements IEngine1<Result, Input> {

	private final IEngine engine;

	public Engine1(IEngine engine) {
		this.engine = engine;
	}

	@Override
	public Result apply(Input input) {
		return engine.transformRaw(input);
	}


}
