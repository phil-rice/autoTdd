package org.autoTdd.internal;

import junit.framework.Assert;

import org.autoTdd.ITyped;

public class EngineType implements ITyped {

	private final Class<?> resultClass;
	private final Class<?>[] parameters;

	public EngineType(Class<?> resultClass, Class<?>[] parameters) {
		this.resultClass = resultClass;
		this.parameters = parameters;
	}

	@Override
	public Class<?> resultClass() {
		return resultClass;
	}

	@Override
	public Class<?>[] parameters() {
		return parameters;
	}

	public void assertTypesMatch(ITyped type) {
		assertMatchesResultClass(type.resultClass());
		assertMatchesInputClasses(type.parameters());
	}

	/** matches if it, or its class, matches */
	public void assertClassMatchesResultClass(Object result) {
		assertMatchesResultClass(result.getClass());
	}

	public void assertMatchesResultClass(Class<?> result) {
		assertIsAssignableFrom(this.resultClass, result);
	}

	private void assertIsAssignableFrom(Class<?> master, Class<?> beingChecked) {
		if (!master.isAssignableFrom(beingChecked))
			throw new IllegalArgumentException();
	}

	/** matches if they, or their class, matches */
	public void assertMatchesInputClasses(Class<?>[] inputClasses) {
		Assert.assertEquals(this.parameters.length, inputClasses.length);
		for (int i = 0; i < inputClasses.length; i++)
			assertIsAssignableFrom(this.parameters[i], inputClasses[i]);
	}

	public void assertClassesMatchesInputClasses(Object[] inputClasses) {
		Assert.assertEquals(this.parameters.length, inputClasses.length);
		for (int i = 0; i < inputClasses.length; i++)
			assertIsAssignableFrom(this.parameters[i], inputClasses[i].getClass());
	}
}
