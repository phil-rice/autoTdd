package org.autoTdd.engine;

public abstract class Constraint<Input, Match, Result> {
	public final Input input;
	public final Result result;

	public Constraint(Input input, Result result) {
		this.input = input;
		this.result = result;
	}

	abstract public Match match(Input input);

	abstract public Result result(Match match);

	@Override
	public String toString() {
		return "Constraint [input=" + input + ", result=" + result + "]";
	}
}