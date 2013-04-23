package org.autoTdd.system.internal;

import junit.framework.TestCase;

import org.autoTdd.IEngineSpecification;
import org.autoTdd.IEngineStrategy;
import org.autoTdd.internal.AutoTddFactory;
import org.autoTdd.internal.Constraint;
import org.autoTdd.internal.EngineSpecification;
import org.softwarefm.utilities.indent.Indent;
import org.softwarefm.utilities.tests.Tests;

public class AutoTddFactoryTest extends TestCase {

	private AutoTddFactory factory;
	private IEngineSpecification spec1;
	private IEngineSpecification spec2;

	public void testReturnsSameObjectIfSameName() {
		assertNotNull(factory.builderFor("name", spec1));
		assertSame(factory.builderFor("name", spec1), factory.builderFor("name", spec1));
		assertSame(factory.builderFor("name1", spec1), factory.builderFor("name1", spec1));
		assertSame(factory.builderFor("otherName", spec2), factory.builderFor("otherName", spec2));

		assertNotSame(factory.builderFor("name", spec1), factory.builderFor("otherName", spec2));
	}

	public void testThrowsExceptionIfDifferentEngineStrategy() {
		factory.builderFor("name", spec1);
		Tests.assertThrows(IllegalArgumentException.class, new Runnable() {
			@Override
			public void run() {
				factory.builderFor("name", spec2);
			}
		});
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		factory = new AutoTddFactory();
		spec1 = new EngineSpecification(Object.class, new Class[0], new DummyEngineStrategy(), "default");
		spec2 = new EngineSpecification(Object.class, new Class[0], new DummyEngineStrategy(), "default");
	}

	static class DummyEngineStrategy implements IEngineStrategy {
		@Override
		public Object makeContext() {
			return null;
		}

		@Override
		public boolean match(Object context, Constraint constraint, Object... input) {
			return false;
		}

		@Override
		public String displayBecause(Indent indent, Constraint constraint) {
			return null;
		}

		@Override
		public void validateConstraint(Constraint constraint) {
		}

	}
}
