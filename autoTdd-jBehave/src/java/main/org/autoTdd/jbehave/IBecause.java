package org.autoTdd.jbehave;

import java.util.Arrays;

import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCreator.ParameterisedStep;
import org.softwarefm.jbehavesample.StepsHelper;
import org.softwarefm.schengen.IEngineSteps;

public interface IBecause {

	public static class Utils {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static boolean execute(IBecause because, Object... parameters) {
			if (because instanceof IBecause2<?, ?>) {
				checkInputs(parameters, 2);
				return ((IBecause2) because).evaluate(parameters[0], parameters[1]);
			}
			throw new IllegalStateException("Don't know how to execute because clause of " + because.getClass());
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public static boolean execute(IBecause because, Step step) {
			if (!(step  instanceof ParameterisedStep))
				throw new IllegalArgumentException("Cannot execute because as step is of " + step.getClass());
			
			IEngineSteps engineSteps = StepsHelper.findEngineStepsFor(step);
			Object[] inputs = engineSteps.getInputs();
			if (because instanceof IBecause2<?, ?>) {
				checkInputs(inputs, 2);
				return ((IBecause2) because).evaluate(inputs[0], inputs[1]);
			}
			throw new IllegalStateException("Don't know how to execute because clause of " + because.getClass());
		}

		private static void checkInputs(Object[] parameters, int expected) {
			if (parameters.length != 2)
				throw new IllegalStateException("Wrong number of parameters, expected " + expected + " had " + parameters.length + " which are " + Arrays.asList(parameters));
		}
	}

}
