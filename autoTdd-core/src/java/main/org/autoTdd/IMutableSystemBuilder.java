package org.autoTdd;

public interface IMutableSystemBuilder extends ISystem {

	void specify(String engineName, IEngineSpecification specification);

	void addConstraint(String engineName, Object result, Object because, Object... inputs);

	ISystem build();
}
