package org.autoTdd.exceptions;

import org.autoTdd.internal.Constraint;

public class ConstraintConflictException extends RuntimeException {

	public ConstraintConflictException(Constraint constraint1, Constraint constraint2) {
	}

}
