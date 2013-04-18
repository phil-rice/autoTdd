package org.autoTdd.groovy;

import org.autoTdd.IEngineSpecification;
import org.autoTdd.builder.IEngineBuilder;
import org.autoTdd.internal.AutoTddFactory;
import org.autoTdd.internal.Constraint;
import org.autoTdd.internal.SystemSpecification;

public class DSL {

	public static IEngineSpecification specification(Class<?> resultClass, Class<?>[] parameters, Object defaultOutput, String... namesOfInput){
		return new SystemSpecification(resultClass, parameters, new GroovyEngineStrategy(namesOfInput), defaultOutput);
	}
	
	public static IEngineBuilder simple(Class<?> resultClass, Class<?> inputClass, Object defaultOutput, String  nameOfInput){
		IEngineSpecification specification = specification(resultClass, new Class[]{inputClass}, defaultOutput, nameOfInput);
		return new AutoTddFactory().builderFor("simple", specification);
	}
	
	public static Constraint constraint(Object result, String because, Object... inputs) {
		return new Constraint(result, because, inputs);
	}

}