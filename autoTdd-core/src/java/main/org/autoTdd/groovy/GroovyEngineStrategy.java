package org.autoTdd.groovy;

import groovy.lang.Binding;
import groovy.lang.Script;

import java.util.concurrent.Callable;

import org.autoTdd.AbstractEngineStrategy;
import org.autoTdd.internal.Constraint;
import org.softwarefm.utilities.indent.Indent;
import org.softwarefm.utilities.maps.Maps;

public class GroovyEngineStrategy extends AbstractEngineStrategy {

	private final String[] namesOfInput;

	public GroovyEngineStrategy(String... namesOfInput) {
		super(String.class);
		this.namesOfInput = namesOfInput;
	}

	@Override
	public String displayBecause(Indent indent, Constraint constraint) {
		return (String) constraint.getBecause();
	}

	@Override
	public boolean match(final Object contextAsObject, final Constraint constraint, Object... input) {
		final GroovyEngineContext context = (GroovyEngineContext) contextAsObject;
		Script script = Maps.findOrCreate(context.scriptCache, constraint, new Callable<Script>() {
			@Override
			public Script call() throws Exception {
				return context.shell.parse((String) constraint.getBecause());
			}
		});
		Binding binding = new Binding();
		if (input.length != namesOfInput.length)
			throw new IllegalArgumentException();
		for (int i = 0; i < namesOfInput.length; i++)
			binding.setVariable(namesOfInput[i], input[i]);
		script.setBinding(binding);
		Object value = script.run();
		if (value == null)
			return false;
		if (!(value instanceof Boolean))
			throw new IllegalStateException("Should be boolean is " + value.getClass() + ": " + value);
		return (Boolean) value;
	}

	@Override
	public GroovyEngineContext makeContext() {
		return new GroovyEngineContext();
	}


}
