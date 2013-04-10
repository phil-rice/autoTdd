package org.autoTdd.system.internal;

import junit.framework.TestCase;

import org.autoTdd.IEngineStrategy;
import org.autoTdd.ISystemSpecification;
import org.autoTdd.internal.AutoTddFactory;
import org.autoTdd.internal.Constraint;
import org.autoTdd.internal.SystemSpecification;
import org.softwarefm.utilities.indent.Indent;
import org.softwarefm.utilities.tests.Tests;

public class AutoTddFactoryTest extends TestCase {

	private AutoTddFactory factory;
	private ISystemSpecification spec1;
	private ISystemSpecification spec2;

	public void testReturnsSameObjectIfSameName() {
		assertNotNull(factory.builderFor(spec1, "name"));
		assertSame(factory.builderFor(spec1, "name"), factory.builderFor(spec1, "name"));
		assertSame(factory.builderFor(spec1, "name1"), factory.builderFor(spec1, "name1"));
		assertSame(factory.builderFor(spec2, "otherName"), factory.builderFor(spec2, "otherName"));

		assertNotSame(factory.builderFor(spec1, "name"), factory.builderFor(spec2, "otherName"));
	}

	public void testThrowsExceptionIfDifferentEngineStrategy() {
		factory.builderFor(spec1, "name");
		Tests.assertThrows(IllegalArgumentException.class, new Runnable() {
			public void run() {
				factory.builderFor(spec2, "name");
			}
		});
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		factory = new AutoTddFactory();
		spec1 = new SystemSpecification(Object.class, new Class[0], new DummyEngineStrategy(), "default");
		spec2 = new SystemSpecification(Object.class, new Class[0], new DummyEngineStrategy(), "default");
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
