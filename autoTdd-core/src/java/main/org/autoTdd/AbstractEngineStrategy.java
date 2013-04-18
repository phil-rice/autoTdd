package org.autoTdd;

import org.autoTdd.internal.Constraint;
import org.softwarefm.utilities.indent.Indent;

public abstract class AbstractEngineStrategy implements IEngineStrategy{
	private Class<?> becauseClass;

	public AbstractEngineStrategy(Class<?> becauseClass) {
		this.becauseClass = becauseClass;
	}
	@Override
	public void validateConstraint(Constraint constraint) {
		if (constraint == null)
			throw new NullPointerException();
		if (constraint.getBecause() == null)
			throw new NullPointerException();
		if (!becauseClass.isAssignableFrom(constraint.getBecause().getClass()))
			throw new IllegalStateException("Because if of class: " + constraint.getClass().getName() +" and it should be a " + becauseClass.getSimpleName());
		
	}
	@Override
	public String displayBecause(Indent indent, Constraint constraint) {
		return constraint.getBecause().toString();
	}


}
