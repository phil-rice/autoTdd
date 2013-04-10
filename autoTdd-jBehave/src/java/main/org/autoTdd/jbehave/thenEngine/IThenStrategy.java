package org.autoTdd.jbehave.thenEngine;

import java.lang.reflect.Method;

import org.jbehave.core.annotations.Then;

public interface IThenStrategy {

	void execute(Then then, Method method, Object result);

}
