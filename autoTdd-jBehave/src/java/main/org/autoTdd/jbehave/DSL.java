package org.autoTdd.jbehave;

import org.autoTdd.ISystemSpecification;
import org.autoTdd.builder.IEngineBuilder;
import org.autoTdd.internal.AutoTddFactory;
import org.autoTdd.internal.SystemSpecification;

public class DSL {
	public static ISystemSpecification specification(Class<?> resultClass, Object defaultOutput, Class<?>... parameters) {
		return new SystemSpecification(resultClass, parameters, new JbehaveEngineStrategy(), defaultOutput);
	}

	public static IEngineBuilder system(String systemName, Class<?> resultClass, Class<?> inputClass, Object defaultOutput) {
		ISystemSpecification specification = specification(resultClass, defaultOutput, new Class[] { inputClass });
		return new AutoTddFactory().builderFor(specification, systemName);
	}


}
