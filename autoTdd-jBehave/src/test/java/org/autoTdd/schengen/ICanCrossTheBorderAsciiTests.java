package org.autoTdd.schengen;

import junit.framework.TestCase;

import org.autoTdd.IEngineStrategy;
import org.autoTdd.ISystem;
import org.autoTdd.engine.IEngine;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.jbehave.JBehaveSessionManager;
import org.autoTdd.jbehave.JbehaveEngineStrategy;
import org.autoTdd.jbehave.internal.BecauseForConstraint;
import org.autoTdd.tests.AsciiArtBuilder;
import org.autoTdd.tests.TreeComparator;
import org.jbehave.core.ConfigurableEmbedder;

public class ICanCrossTheBorderAsciiTests extends TestCase {

	private AsciiArtBuilder builder;
	private IEngineStrategy engineStrategy = new JbehaveEngineStrategy();

	public void testICanCrossTheBorder() {
		checkClass(ICanCrossTheBorder.class, "" + //
				"if normal/schengen,deborah" + //
				"  if drugs/schengen,gary then arrest" + //
				"  else if money/schengen,francisco then detain " + //
				"       else allow " + //
				"else default");

	}

	@SuppressWarnings("unchecked")
	private void checkClass(Class<? extends ConfigurableEmbedder> clazz, String string) {
		IEngineAsTree actual = (IEngineAsTree) JBehaveSessionManager.makeEnginesFor(clazz).engine("I assess the person");
		IEngineAsTree expected = builder.build(string, engineStrategy, String.class, Schengen.class, Person.class);
		assertEquals(actual.toString(), null, new TreeComparator().describeMisMatch(expected, actual));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Person deborah = new Person();
		deborah.setName("Deborah");

		Person francisco = new Person();
		francisco.setName("Francisco");
		francisco.setMoney(AccessSteps.aLotOfMoney);

		Person gary = new Person();
		gary.setName("Gary");
		gary.setMoney(AccessSteps.aLotOfMoney);
		gary.setCarryingDrugs(true);

		builder = new AsciiArtBuilder();
		builder.registerBecause("normal", new BecauseForConstraint(new BecauseDefault(), "I said so", "Deborah", new Object[0]));
		builder.registerBecause("money", new BecauseForConstraint(new BecauseMoney(), "the person has a lot of money", "Francisco", new Object[0]));
		builder.registerBecause("drugs", new BecauseForConstraint(new BecauseDrugs(), "the person is carrying drugs", "Gary", new Object[0]));

		builder.registerInput("deborah", deborah);
		builder.registerInput("francisco", francisco);
		builder.registerInput("gary", gary);
		builder.registerInput("schengen", new Schengen());

		builder.registerResult("arrest", "arrested");
		builder.registerResult("detain", "detained");
		builder.registerResult("allow", "allowed");
		builder.registerResult("default", "Default");
		// builder.registerBecause(becauseString, becauseObject);
	}
}
