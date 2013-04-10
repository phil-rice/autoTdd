package org.autoTdd;

import java.util.List;

import org.autoTdd.builder.IEngineBuilder;

public interface IMutableSystemBuilder {

	void specify(String systemName, ISystemSpecification specification);

	IEngineBuilder builderFor(String systemName);
	
	List<String> systemNames();

	 void addConstraint(String systemName, Object result, Object because, Object... inputs);
}
