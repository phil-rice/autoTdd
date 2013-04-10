package org.autoTdd.builder;

import org.autoTdd.ITyped;
import org.autoTdd.engine.IEngine1;
import org.autoTdd.engine.IEngine2;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.internal.Constraint;

/**
 * The engine builder is the interface that allows an engine to be created.
 * 
 * The engine builder is immutable so when you call add: make sure you use the result!
 * 
 * @author Phil
 * 
 */
public interface IEngineBuilder extends ITyped {

	IEngineBuilder add(Constraint... constraints);

	IEngineBuilder addConstraint(Object result, Object because, Object... inputs);

	<Result, Input> IEngine1<Result, Input> engine1(Class<? extends Result> resultClass, Class<? extends Input> inputClass);

	<Result, Input1, Input2> IEngine2<Result, Input1, Input2> engine2(Class<? extends Result> resultClass, Class<? extends Input1> input1Class, Class<? extends Input2> input2Class);

	IEngineAsTree tree();

}
