package org.autoTdd.builder.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.autoTdd.IEngineSpecification;
import org.autoTdd.builder.IEngineBuilder;
import org.autoTdd.engine.IEngine1;
import org.autoTdd.engine.IEngine2;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.engine.internal.Engine;
import org.autoTdd.engine.internal.Engine1;
import org.autoTdd.engine.internal.Engine2;
import org.autoTdd.exceptions.ConstraintConflictException;
import org.autoTdd.internal.AbstractNodeHolder;
import org.autoTdd.internal.Constraint;

public class EngineBuilder extends AbstractNodeHolder implements IEngineBuilder {

	private final List<Constraint> constraints;

	public EngineBuilder(IEngineSpecification specification) {
		this(specification, new ArrayList<Constraint>());
	}

	public EngineBuilder(IEngineSpecification specification, List<Constraint> constraints) {
		super(specification);
		this.constraints = constraints;
		assertClassMatchesResultClass(defaultOutput);
		for (Constraint constraint : constraints)
			assertTypesMatch(constraint);
	}

	@Override
	public IEngineBuilder addConstraint(Object result, Object because, Object... inputs) {
		return add(new Constraint(result, because, inputs));
	}

	@Override
	public IEngineBuilder add(Constraint... constraints) {
			final List<Constraint> newConstraints = new ArrayList<Constraint>(this.constraints);
			for (Constraint constraint : constraints) {
				try {
				engineStrategy.validateConstraint(constraint);
				assertTypesMatch(constraint);
				assertClassesMatchesInputClasses(constraint.getInputs());
				assertClassMatchesResultClass(constraint.getResult());

				newConstraints.add(constraint);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Constrains: " + constraint, e);
				}
			}
			return new EngineBuilder(specification, newConstraints);
	}

	@Override
	public IEngineAsTree tree() {
		Node root = null;
		for (Constraint constraint : constraints)
			if (root == null)
				root = new Node(constraint, null, null, null);
			else {
				Node leafNode = findNode(root, constraint.getInputs());
				if (leafNode == null) {
					Node lastAllFalseNode = findAllfalseNode(root);
					lastAllFalseNode.noMatch = new Node(constraint, null, null, null);
				} else {
					boolean leafMatchesConstraint = engineStrategy.match(context, constraint, leafNode.constraint.getInputs());
					if (leafMatchesConstraint)
						throw new ConstraintConflictException(leafNode.constraint, constraint); // there is nothing in the condition to differentiate us
					boolean match = engineStrategy.match(context, leafNode.constraint, constraint.getInputs());
					if (match)
						leafNode.match = new Node(constraint, leafNode, null, null);
					else
						leafNode.noMatch = new Node(constraint, null, null, null);
				}
			}
		return new Engine(specification, root);
	}

	@Override
	public <Result, Input> IEngine1<Result, Input> engine1(Class<? extends Result> resultClass, Class<? extends Input> inputClass) {
		assertMatchesInputClasses(new Class<?>[] { inputClass });
		assertMatchesResultClass(resultClass);

		return new Engine1<Result, Input>(tree());
	}

	@Override
	public <Result, Input1, Input2> IEngine2<Result, Input1, Input2> engine2(Class<? extends Result> resultClass, Class<? extends Input1> input1Class, Class<? extends Input2> input2Class) {
		assertMatchesInputClasses(new Class<?>[] { input1Class, input2Class });
		assertMatchesResultClass(resultClass);

		return new Engine2<Result, Input1, Input2>(tree());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +"\n "+asString(tree().getRoot());
	}

}
