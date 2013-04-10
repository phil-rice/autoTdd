package org.softwarefm.jbehavesample.exceptions;

import java.lang.reflect.Method;

import org.jbehave.core.annotations.Then;

public class CannotExecuteThenException extends RuntimeException {

	public CannotExecuteThenException(Then then, Method method, Object result) {
		super("Cannot execute then " + then + ", " + method + ", " + result);
	}

}
