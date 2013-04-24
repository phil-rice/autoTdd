package org.autoTdd;

import static org.autoTdd.ConstraintsForTests.*;
import static org.autoTdd.ConstraintsForTests.bigPos;
import static org.autoTdd.ConstraintsForTests.neg;
import static org.autoTdd.ConstraintsForTests.pos;
import static org.autoTdd.ConstraintsForTests.vBigPos;
import junit.framework.TestCase;

import org.autoTdd.builder.IEngineBuilder;
import org.autoTdd.engine.IEngineAsTree;
import org.autoTdd.exceptions.ConstraintConflictException;
import org.autoTdd.groovy.DSL;
import org.autoTdd.groovy.GroovyEngineStrategy;
import org.autoTdd.internal.Constraint;
import org.autoTdd.tests.AsciiArtBuilder;
import org.autoTdd.tests.TreeComparator;
import org.softwarefm.utilities.tests.Tests;

public class AsciiArtEngineTests extends TestCase {

	private AsciiArtBuilder builder;
	private GroovyEngineStrategy engineStrategy = new GroovyEngineStrategy("x");
	private IEngineBuilder engineBuilder;

	public void testGroovyBuilder() {
		IEngineAsTree tree = build("if pos then pos else default");
		assertEquals("Positive", tree.transformRaw(1));
		assertEquals("default", tree.transformRaw(-1));
	}

	public void testEngine() {
		check("if pos/1 then pos else default", pos);
		check("if pos/1 if bigPos/20 then bigPos else pos else default", pos, bigPos);
		check("if bigPos/20 then bigPos else if pos/1 then pos else default", bigPos, pos);
		check("if pos/1 if bigPos/20 if vBigPos/2000 then vBigPos else bigPos  else pos else default", pos, bigPos, vBigPos);
		check("if pos/1 if vBigPos/2000 then vBigPos else if bigPos/20 then bigPos else pos  else default", pos, vBigPos, bigPos);
		check("if vBigPos/2000 then vBigPos else if bigPos/20 then bigPos else if pos/1 then pos else default", vBigPos, bigPos, pos);
	}
	
	public void testAddingWhenCannotDifferentiate(){
		//pos2 cannot be differentiated from pos
		checkConflict( pos, pos2, pos, pos2);
		checkConflict( pos, pos2, bigPos, neg, pos, pos2);
		
	}

	private void checkConflict(Constraint conflict1, Constraint conflict2, Constraint... constraints) {
		final IEngineBuilder builder = engineBuilder.add(constraints);
		ConstraintConflictException e = Tests.assertThrows(ConstraintConflictException.class, new Runnable() {
			public void run() {
				builder.tree();
			}
		});
		assertSame(conflict1, e.constraint1);
		assertSame(conflict2, e.constraint2);
	}

	private void check(String tree, Constraint... constraints) {
		IEngineAsTree expected =build(tree);
		IEngineAsTree actual = engineBuilder.add(constraints).tree();
		assertEquals(actual.toString(), null, new TreeComparator().describeMisMatch(expected, actual));
	}

	private IEngineAsTree build(String string) {
		return builder.build(string, engineStrategy, String.class, Integer.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		engineBuilder = DSL.simple(String.class, Integer.class, "default", "x");
		builder = new AsciiArtBuilder();
		builder.registerBecause("pos", pos.getBecause());
		builder.registerBecause("bigPos", bigPos.getBecause());
		builder.registerBecause("vBigPos", vBigPos.getBecause());
		builder.registerBecause("neg", neg.getBecause());
		builder.registerBecause("bigNeg", bigNeg.getBecause());

		builder.registerInput("1", 1);
		builder.registerInput("20", 20);
		builder.registerInput("2000", 2000);

		builder.registerResult("default", "default");
		builder.registerResult("pos", "Positive");
		builder.registerResult("bigPos", "BigPositive");
		builder.registerResult("vBigPos", "VBigPositive");
		builder.registerResult("y", "Y");
		builder.registerResult("z", "Z");
	}

}
