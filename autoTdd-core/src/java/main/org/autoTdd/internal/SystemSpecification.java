package org.autoTdd.internal;

import org.autoTdd.IEngineStrategy;
import org.autoTdd.ISystemSpecification;

public class SystemSpecification extends EngineType implements ISystemSpecification {

	private final IEngineStrategy engineStrategy;
	private final Object defaultOutput;

	public SystemSpecification(Class<?> resultClass, Class<?>[] parameters, IEngineStrategy engineStrategy, Object defaultOutput) {
		super(resultClass, parameters);
		this.engineStrategy = engineStrategy;
		this.defaultOutput = defaultOutput;
	}

	@Override
	public IEngineStrategy engineStrategy() {
		return engineStrategy;
	}

	@Override
	public Object defaultOutput() {
		return defaultOutput;
	}

}
