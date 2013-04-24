package org.autoTdd.builder.internal;

import java.util.ArrayList;
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
				throw new IllegalArgumentException(constraint.toString(), e);
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
			else
				addConstraint(root, constraint);
		return new Engine(specification, root);
	}

	private void addConstraint(Node root, Constraint constraint) {
		Node lastMatchNode = findLastMatchNode(root, constraint.getInputs());
		Node newNode = new Node(constraint, lastMatchNode, null, null);
		if (lastMatchNode == null || lastMatchNode.match != null) {
			Node base = lastMatchNode == null ? root : lastMatchNode.match;
			Node lastAllFalseNode = findAllfalseNode(base);
			lastAllFalseNode.noMatch = new Node(constraint, lastMatchNode, null, null);
		} else {
			boolean lastMatchNodeAlsoMatchesConstraint = engineStrategy.match(context, constraint, lastMatchNode.constraint.getInputs());
			if (lastMatchNodeAlsoMatchesConstraint)
				throw new ConstraintConflictException( lastMatchNode.constraint, constraint);
			lastMatchNode.match = newNode;
		}
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
		return getClass().getSimpleName() + "\n " + asString(tree().getRoot());
	}

}
