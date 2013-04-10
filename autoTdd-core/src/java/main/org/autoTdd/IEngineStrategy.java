package org.autoTdd;

import org.autoTdd.internal.Constraint;
import org.softwarefm.utilities.indent.Indent;

public interface IEngineStrategy  {
	
	void validateConstraint(Constraint constraint);

	String displayBecause(Indent indent, Constraint constraint);
	
	Object makeContext();
	
	boolean match(Object context, final Constraint constraint, Object... input);

}
