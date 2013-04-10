package org.autoTdd;

public interface ITyped {

	Class<?> resultClass();

	Class<?>[] parameters();

	void assertTypesMatch(ITyped type);
}
