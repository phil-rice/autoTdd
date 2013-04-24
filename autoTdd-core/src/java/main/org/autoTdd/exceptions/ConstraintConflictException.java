package org.autoTdd.exceptions;

import org.autoTdd.internal.Constraint;

public class ConstraintConflictException extends RuntimeException {

	public Constraint constraint1;
	public Constraint constraint2;

	public ConstraintConflictException(Constraint constraint1, Constraint constraint2) {
		this.constraint1 = constraint1;
		this.constraint2 = constraint2;
	}

}
