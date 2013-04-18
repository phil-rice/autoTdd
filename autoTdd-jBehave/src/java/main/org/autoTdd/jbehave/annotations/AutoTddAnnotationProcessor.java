package org.autoTdd.jbehave.annotations;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.autoTdd.IEngineSpecification;
import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.ISystem;
import org.autoTdd.engine.IEngine;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.jbehave.because.IBecause;
import org.autoTdd.jbehave.internal.BecauseForConstraint;
import org.autoTdd.steps.AutoTddStepTypes;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.annotations.Because;
import org.jbehave.core.annotations.Called;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.AnnotationProcessor;
import org.jbehave.core.configuration.DefaultAnnotationStrategy;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepType;
import org.softwarefm.jbehavesample.StepsHelper;
import org.softwarefm.schengen.IEngineSteps;
import org.softwarefm.utilities.clone.ICloner;

public class AutoTddAnnotationProcessor extends AnnotationProcessor implements IMutableSystemBuilder {

	private final ICloner cloner;
	private String lastEngineName;
	private List<Object> lastThens = new ArrayList<Object>();;
	private String lastThenText;
	private Class<? extends ConfigurableEmbedder> clazz;
	private boolean hasBuilt;
	private IMutableSystemBuilder systemBuilder;

	public ISystem build() {
		return systemBuilder.build();
	}

	@Override
	public IEngine engine(String engineName) {
		return systemBuilder.engine(engineName);
	}

	@Override
	public IEngineAsTree tree(String engineName) {
		return systemBuilder.tree(engineName);
	}

	public AutoTddAnnotationProcessor(IMutableSystemBuilder systemBuilder, final ICloner cloner) {
		this.systemBuilder = systemBuilder;
		if (systemBuilder == null)
			throw new NullPointerException();
		this.cloner = cloner;
		if (cloner == null)
			throw new NullPointerException();
		register(Given.class, new DefaultAnnotationStrategy<Given>(StepType.GIVEN) {
			@Override
			public int priority(Given annotation) {
				return annotation.priority();
			}

			@Override
			public String value(Given annotation) {
				return annotation.value();
			}

			@Override
			public Object execute(Step step, Method method, Given given, Object object, Object[] parameters) throws Exception {
				lastThens = new ArrayList<Object>();
				return super.execute(step, method, given, object, parameters);
			}
		});
		register(When.class, new DefaultAnnotationStrategy<When>(StepType.WHEN) {
			@Override
			public int priority(When annotation) {
				return annotation.priority();
			}

			@Override
			public String value(When annotation) {
				return annotation.value();
			}

			@Override
			public Object execute(Step step, Method method, When when, Object object, Object[] parameters) throws Exception {
				lastEngineName = when.value();
				return super.execute(step, method, when, object, parameters);
			}
		});
		register(Because.class, new DefaultAnnotationStrategy<Because>(AutoTddStepTypes.BECAUSE) {
			@Override
			public int priority(Because annotation) {
				return annotation.priority();
			}

			@Override
			public String value(Because annotation) {
				return annotation.value();
			}

			@Override
			public Object execute(Step step, Method method, Because annotation, Object object, Object[] parameters) throws Exception {
				Object result = super.execute(step, method, annotation, object, parameters);
				if (!(result instanceof IBecause)) {
					throw new IllegalStateException("Results from @Because methods must be of type IBecause. Offending method is " + method);
				}
				IBecause because = (IBecause) result;
				boolean actual = IBecause.Utils.execute(because, step);
				if (!actual)
					throw new IllegalStateException("The because was not true for the parameters. Offending method is " + method + " parameters are " + Arrays.asList(parameters));

				// System.out.println("Running " + method);
				IEngineSteps engineSteps = StepsHelper.findEngineStepsFor(step);
				// System.out.println("  LastEngineName " + lastEngineName);
				// System.out.println("  lastThen " + lastThen);
				// System.out.println("  result " + result);
				if (engineSteps != null) {
					Object[] inputs = engineSteps.getInputs();
					// System.out.println("  Context " + context);
					// System.out.println("  Value " + value);
					Object[] copyOfInputs = ICloner.Utils.copyArray(cloner, inputs);
					BecauseForConstraint becauseForConstraint = new BecauseForConstraint((IBecause) result, annotation, StepsHelper.findParametersFor(step));
					addConstraint(lastEngineName, then(), becauseForConstraint, copyOfInputs);
				}
				return result;
			}

			private Object then() {
				final List<Runnable> runnables = new ArrayList<Runnable>();
				for (Object then : lastThens)
					if (then instanceof Runnable)
						runnables.add((Runnable) then);
				if (runnables.size() == 0)
					switch (lastThens.size()) {
					case 0:
						throw new IllegalStateException("No Thens clause ");
					case 1:
						return lastThens.get(0);
					default:
						throw new IllegalStateException("Too many Thens. " + lastThens);
					}
				if (runnables.size() != lastThens.size()) {
					List<Object> otherThens = new ArrayList<Object>(lastThens);
					otherThens.removeAll(runnables);
					throw new IllegalStateException("Have Runnables and other Thens. Other Thens are: " + otherThens);
				}
				if (runnables.size() == 1)
					return runnables.get(0);
				return new Runnable() {
					public void run() {
						for (Runnable runnable : runnables)
							runnable.run();
					}
				};
			}

		});
		register(Then.class, new DefaultAnnotationStrategy<Then>(StepType.THEN) {
			@Override
			public int priority(Then annotation) {
				return annotation.priority();
			}

			@Override
			public String value(Then annotation) {
				return annotation.value();
			}

			@Override
			public Object execute(Step step, Method method, Then then, Object object, Object[] parameters) throws Exception {
				lastThenText = then.value() + Arrays.asList(StepsHelper.findParametersFor(step));
				Object result = super.execute(step, method, then, object, parameters);
				if (result != null)
					lastThens.add(result);
				if (result instanceof Runnable)
					((Runnable) result).run();
				return result;
			}
		});
		register(Called.class, new DefaultAnnotationStrategy<Called>(AutoTddStepTypes.CALLED) {
			@Override
			public int priority(Called annotation) {
				return annotation.priority();
			}

			@Override
			public String value(Called annotation) {
				return annotation.value();
			}

		});
	}

	@Override
	public void specify(String engineName, IEngineSpecification specification) {
		systemBuilder.specify(engineName, specification);
	}

	@Override
	public List<String> engineNames() {
		return systemBuilder.engineNames();
	}

	@Override
	public void addConstraint(String systemName, Object result, Object because, Object... inputs) {
		if (systemName == null)
			throw new NullPointerException();
		systemBuilder.addConstraint(systemName, result, because, inputs);
	}

}