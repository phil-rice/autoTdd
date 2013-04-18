package org.softwarefm.jbehavesample;

import org.jbehave.core.annotations.Because;
import org.jbehave.core.annotations.Called;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public interface MethodAnnotationVisitor {

	void visit(Given given);

	void visit(Because because);

	void visit(When when);

	void visit(Then then);

	void visit(Called called);

}
