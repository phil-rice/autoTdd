package org.autoTdd.jbehave.annotations;

import org.jbehave.core.annotations.Because;
import org.jbehave.core.annotations.Called;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.softwarefm.jbehavesample.MethodAnnotationVisitor;

public class MethodAnnotationAdapter implements MethodAnnotationVisitor{

	@Override
	public void visit(Given given) {
	}

	@Override
	public void visit(Because because) {
	}

	@Override
	public void visit(When when) {
	}

	@Override
	public void visit(Then then) {
	}

	@Override
	public void visit(Called called) {
	}
}
