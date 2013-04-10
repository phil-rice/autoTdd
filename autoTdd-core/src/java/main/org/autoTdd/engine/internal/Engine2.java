package org.autoTdd.engine.internal;

import org.autoTdd.engine.IEngine;
import org.autoTdd.engine.IEngine2;

public class Engine2<Result, Input1, Input2> implements IEngine2<Result, Input1, Input2> {

	private final IEngine engine;

	public Engine2(IEngine engine) {
		this.engine = engine;
	}

	@Override
	public Result transform(Input1 input1, Input2 input2) {
		return engine.transformRaw(input1, input2);
	}

}
