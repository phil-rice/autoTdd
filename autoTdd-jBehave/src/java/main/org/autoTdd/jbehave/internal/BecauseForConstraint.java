package org.autoTdd.jbehave.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.autoTdd.jbehave.because.IBecause;
import org.jbehave.core.annotations.Because;

public class BecauseForConstraint {

	public IBecause because;
	public Because becauseAnnotation;
	public List<Object> parameters;

	public BecauseForConstraint(IBecause because, Because becauseAnnotation, Object[] parameters) {
		this.because = because;
		this.becauseAnnotation = becauseAnnotation;
		this.parameters = new ArrayList<Object>(Arrays.asList(parameters));
	}

}
