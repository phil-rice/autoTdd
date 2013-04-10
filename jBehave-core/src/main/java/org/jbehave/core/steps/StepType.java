package org.jbehave.core.steps;

/**
 * Enum representing the step types
 */
public class StepType implements Comparable<StepType>  {
	public static final StepType GIVEN = new StepType("GIVEN");
	public static final StepType WHEN = new StepType("WHEN");
	public static final StepType THEN = new StepType("THEN");
	public static final StepType AND = new StepType("AND");
	public static final StepType IGNORABLE = new StepType("IGNORABLE");

	private String name;

	public StepType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public String name() {
		return name;
	}

	public int compareTo(StepType o) {
		return name.compareTo(o.name);
	}
	
	

}
