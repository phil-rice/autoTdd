package org.autoTdd.exceptions;

import org.autoTdd.internal.Constraint;

public class ConstraintConflictException extends RuntimeException {

	@SuppressWarnings("rawtypes")
	public ConstraintConflictException(Constraint constraint1, Constraint constraint2) {
	}

}
