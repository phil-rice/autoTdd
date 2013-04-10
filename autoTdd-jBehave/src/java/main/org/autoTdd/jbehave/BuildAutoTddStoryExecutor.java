package org.autoTdd.jbehave;

import java.util.Arrays;
import java.util.List;

import org.autoTdd.IAutoTddFactory;
import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.ISystemSpecification;
import org.autoTdd.builder.internal.SystemBuilder;
import org.autoTdd.jbehave.annotations.Because;
import org.autoTdd.jbehave.annotations.MethodAnnotationAdapter;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryRunner.State;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryExecutor;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Step;
import org.softwarefm.jbehavesample.StepsHelper;
import org.softwarefm.schengen.IEngineSteps;
import org.softwarefm.utilities.clone.ICloner;
import org.softwarefm.utilities.exceptions.WrappedException;

//TODO Can remove this extension point, and do everything with the AnnotationProcessor
public class BuildAutoTddStoryExecutor extends SystemBuilder implements IMutableSystemBuilder, StoryExecutor {
	private final ICloner cloner;
	private String lastEngineName;
	private Object lastThen;
	private String lastThenText;
	private Class<? extends ConfigurableEmbedder> clazz;
	private boolean hasBuilt;

	public BuildAutoTddStoryExecutor(IAutoTddFactory autoTddFactory, ICloner cloner, Class<? extends ConfigurableEmbedder> clazz) {
		super(autoTddFactory);
		this.cloner = cloner;
		this.clazz = clazz;
	}

	@Override
	protected void preBuilderFor() {
		try {
			if (!hasBuilt) {
				hasBuilt = true;
				ConfigurableEmbedder newInstance = clazz.newInstance();
				build(newInstance);
			}
		} catch (Exception e) {
			throw WrappedException.wrap(e);
		}
	}

	@Override
	public void specify(String systemName, ISystemSpecification specification) {
		if (hasBuilt)
			throw new IllegalStateException("Cannot specify after build");
		super.specify(systemName, specification);
	}

	@Override
	public State run(State state, final Step step) {
		try {
			final Object result = StepsHelper.parameteriseAndRun(step);
			StepsHelper.visitMethodAnnotation(step, new MethodAnnotationAdapter() {
				public void visit(Given given) {
					lastEngineName = null;
					lastThenText = null;
				};

				@Override
				public void visit(Then then) {
					lastThenText = then.value() + Arrays.asList(StepsHelper.findParametersFor(step));
					lastThen = result;
				}

				@Override
				public void visit(When when) {
					lastEngineName = when.value();
				}

				@Override
				public void visit(Because because) {
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
						BecauseForConstraint becauseForConstraint = new BecauseForConstraint((IBecause) result, because, StepsHelper.findParametersFor(step));
						addConstraint(lastEngineName, lastThen, becauseForConstraint, copyOfInputs);
					}
				}

			});
			return state;
		} catch (Exception e) {
			throw WrappedException.wrap(e);
		}
	}

	public void build(ConfigurableEmbedder iCanCrossTheBorder) {
		try {
			Story story = StepsHelper.getStoryFor(iCanCrossTheBorder);
			Configuration configuration = iCanCrossTheBorder.configuration().useStoryExecutor(this);
			List<CandidateSteps> candidateSteps = iCanCrossTheBorder.stepsFactory().createCandidateSteps();
			iCanCrossTheBorder.configuredEmbedder().storyRunner().run(configuration, candidateSteps, story);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}

}