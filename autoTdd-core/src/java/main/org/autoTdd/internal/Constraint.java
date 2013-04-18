package org.autoTdd.internal;

import java.util.Arrays;

import org.softwarefm.utilities.arrays.ArrayHelper;

public class Constraint extends EngineType {

	private final Object result;
	private final Object because;
	private final Object[] inputs;

	public Constraint(Object result, Object because, Object[] inputs) {
		super(result.getClass(), ArrayHelper.classArray(inputs));
		assertClassesMatchesInputClasses(inputs);
		assertClassMatchesResultClass(result);
		this.result = result;
		this.because = because;
		this.inputs = inputs;
	}

	public Object getResult() {
		return result;
	}

	public Object getBecause() {
		return because;
	}

	public Object[] getInputs() {
		return inputs;
	}

	@Override
	public String toString() {
		return "Constraint [result=" + result + ", because=" + because + ", inputs=" + Arrays.toString(inputs) + "]";
	}

}
