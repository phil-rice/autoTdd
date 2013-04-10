package org.autoTdd;

import org.autoTdd.builder.IEngineBuilder;

public interface IAutoTddFactory {

	 IEngineBuilder builderFor(ISystemSpecification specification, String systemName);

}
