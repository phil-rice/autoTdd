package org.autoTdd.tests;

import org.autoTdd.IEngineStrategy;
import org.autoTdd.internal.Constraint;
import org.softwarefm.utilities.indent.Indent;

public class NullEngineStrategy implements IEngineStrategy {

	@Override
	public void validateConstraint(Constraint constraint) {
	}

	@Override
	public boolean match(Object context, Constraint constraint, Object... input) {
		return false;
	}

	@Override
	public Object makeContext() {
		return null;
	}

	@Override
	public String displayBecause(Indent indent, Constraint constraint) {
		return constraint.getBecause().toString();
	}
}