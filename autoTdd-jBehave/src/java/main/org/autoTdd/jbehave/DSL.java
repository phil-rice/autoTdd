package org.autoTdd.jbehave;

import org.autoTdd.IEngineSpecification;
import org.autoTdd.builder.IEngineBuilder;
import org.autoTdd.internal.AutoTddFactory;
import org.autoTdd.internal.SystemSpecification;

public class DSL {
	public static IEngineSpecification specification(Class<?> resultClass, Object defaultOutput, Class<?>... parameters) {
		return new SystemSpecification(resultClass, parameters, new JbehaveEngineStrategy(), defaultOutput);
	}

	public static IEngineBuilder system(String systemName, Class<?> resultClass, Class<?> inputClass, Object defaultOutput) {
		IEngineSpecification specification = specification(resultClass, defaultOutput, new Class[] { inputClass });
		return new AutoTddFactory().builderFor(systemName, specification);
	}


}
