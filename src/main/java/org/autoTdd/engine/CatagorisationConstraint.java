package org.autoTdd.engine;


public abstract class CatagorisationConstraint<Input, Result> extends Constraint<Input, Result, Result> {

	public CatagorisationConstraint(Input input, Result result) {
		super(input, result);
	}

	public Result result(Result match) {
		return match;
	};
}