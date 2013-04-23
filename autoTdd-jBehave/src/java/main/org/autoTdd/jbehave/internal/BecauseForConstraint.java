package org.autoTdd.jbehave.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.autoTdd.jbehave.because.IBecause;
import org.jbehave.core.annotations.Because;

public class BecauseForConstraint {

	public IBecause because;
	public String becauseAnnotationValue;
	public List<Object> parameters;
	public String called;

	public BecauseForConstraint(IBecause because, String becauseAnnotationValue, String called, Object[] parameters) {
		this.because = because;
		this.becauseAnnotationValue = becauseAnnotationValue;
		this.called = called;
		this.parameters = new ArrayList<Object>(Arrays.asList(parameters));
	}

	public String situationString() {
		if (called != null)
			return called;
		else
			return Arrays.asList(parameters).toString();
	}

	@Override
	public String toString() {
		return "BecauseForConstraint [because=" + because + ", becauseAnnotation=" + becauseAnnotationValue + ", parameters=" + parameters + ", called=" + called + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((because == null) ? 0 : because.hashCode());
		result = prime * result + ((becauseAnnotationValue == null) ? 0 : becauseAnnotationValue.hashCode());
		result = prime * result + ((called == null) ? 0 : called.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BecauseForConstraint other = (BecauseForConstraint) obj;
		if (because == null) {
			if (other.because != null)
				return false;
		} else if (!because.equals(other.because))
			return false;
		if (becauseAnnotationValue == null) {
			if (other.becauseAnnotationValue != null)
				return false;
		} else if (!becauseAnnotationValue.equals(other.becauseAnnotationValue))
			return false;
		if (called == null) {
			if (other.called != null)
				return false;
		} else if (!called.equals(other.called))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}

}
