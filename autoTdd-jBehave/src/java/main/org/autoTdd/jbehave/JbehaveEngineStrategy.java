package org.autoTdd.jbehave;

import org.autoTdd.AbstractEngineStrategy;
import org.autoTdd.internal.Constraint;
import org.softwarefm.utilities.indent.Indent;

public class JbehaveEngineStrategy extends AbstractEngineStrategy {

	public JbehaveEngineStrategy() {
		super(BecauseForConstraint.class);
	}

	@Override
	public Object makeContext() {
		return null;
	}

	@Override
	public String displayBecause(Indent indent, Constraint constraint) {
		BecauseForConstraint because = (BecauseForConstraint) constraint.getBecause();
		return because.becauseAnnotation.value() + because.parameters;
	}

	@Override
	public boolean match(Object context, Constraint constraint, Object... input) {
		BecauseForConstraint becauseForConstraint = (BecauseForConstraint) constraint.getBecause();
		IBecause because = becauseForConstraint.because;
		return IBecause.Utils.execute(because, input);
	}

}
