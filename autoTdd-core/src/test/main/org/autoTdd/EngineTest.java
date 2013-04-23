package org.autoTdd;

import static org.autoTdd.ConstraintsForTests.bigNeg;
import static org.autoTdd.ConstraintsForTests.bigPos;
import static org.autoTdd.ConstraintsForTests.neg;
import static org.autoTdd.ConstraintsForTests.pos;
import static org.autoTdd.ConstraintsForTests.vBigNeg;
import static org.autoTdd.ConstraintsForTests.vBigPos;
import junit.framework.TestCase;

import org.autoTdd.engine.IEngine1;
import org.autoTdd.groovy.DSL;
import org.autoTdd.internal.Constraint;

public class EngineTest extends TestCase {

	public void testEngineWithNoConstraints() {
		IEngine1<String, Integer> engine = DSL.simple(String.class, Integer.class, "Default", "x").engine1(String.class, Integer.class);
		assertEquals("Default", engine.apply(1));
		assertEquals("Default", engine.apply(100));
		assertEquals("Default", engine.apply(-100));
		assertEquals("Default", engine.apply(0));
	}

	public void testWithOneConstraint() {
		IEngine1<String, Integer> engine = DSL.simple(String.class, Integer.class, "Negative", "x").add(pos).engine1(String.class, Integer.class);
		assertEquals("Positive", engine.apply(1));
		assertEquals("Positive", engine.apply(100));
		assertEquals("Negative", engine.apply(-100));
		assertEquals("Negative", engine.apply(0));
	}

	public void testWithTwoConstraint() {
		makeAndCheckPosBigPos(pos, bigPos);
		makeAndCheckPosBigPos(bigPos, pos);
	}

	public void testWithFourConstraints() {
		// checking lots of different orders
		makeAndCheckPosBigPosNegBigNeg(pos, bigPos, neg, bigNeg);
		makeAndCheckPosBigPosNegBigNeg(bigPos, pos, bigNeg, neg);
		makeAndCheckPosBigPosNegBigNeg(neg, bigNeg, pos, bigPos);
		makeAndCheckPosBigPosNegBigNeg(bigNeg, neg, bigPos, pos);
	}

	public void testWithEvenMoreConstraints() {
		// checking lots of different orders
		makeAndCheckvBigPosAndNeg(pos, bigPos, neg, bigNeg, vBigNeg, vBigPos);
		makeAndCheckvBigPosAndNeg(bigPos, pos, vBigNeg, vBigPos, bigNeg, neg);
		makeAndCheckvBigPosAndNeg(bigPos, pos, bigNeg, neg, vBigNeg, vBigPos);
	}

	private void makeAndCheckPosBigPos(Constraint... constraints) {
		IEngine1<String, Integer> engine = DSL.simple(String.class, Integer.class, "ZeroOrNegative", "x").add(constraints).engine1(String.class, Integer.class);
		checkPosAndBigPos(engine);
		String engineString = engine.toString();
		assertEquals(engineString, "ZeroOrNegative", engine.apply(0));
		assertEquals(engineString, "ZeroOrNegative", engine.apply(-1));
		assertEquals(engineString, "ZeroOrNegative", engine.apply(-100));
	}

	private void makeAndCheckPosBigPosNegBigNeg(Constraint... constraints) {
		IEngine1<String, Integer> engine = DSL.simple(String.class, Integer.class, "Zero", "x").add(constraints).engine1(String.class, Integer.class);
		assertEquals("Zero", engine.apply(0));

		checkPosAndBigPos(engine);
		checkNegAndBigNeg(engine);

	}

	private void makeAndCheckvBigPosAndNeg(Constraint... constraints) {
		IEngine1<String, Integer> engine = DSL.simple(String.class, Integer.class, "Zero", "x").add(constraints).engine1(String.class, Integer.class);
		assertEquals("Zero", engine.apply(0));

		checkPosAndVBigPos(engine);
		checkNegAndVBigNeg(engine);

	}

	private void checkNegAndVBigNeg(IEngine1<String, Integer> engine) {
		checkNegAndBigNeg(engine);
		String engineString = engine.toString();
		assertEquals(engineString, "VBigNegative", engine.apply(-3000));

	}

	private void checkPosAndVBigPos(IEngine1<String, Integer> engine) {
		checkPosAndBigPos(engine);
		String engineString = engine.toString();
		assertEquals(engineString, "VBigPositive", engine.apply(3000));
	}

	private void checkNegAndBigNeg(IEngine1<String, Integer> engine) {
		String engineString = engine.toString();
		assertEquals(engineString, "BigNegative", engine.apply(-100));
		assertEquals(engineString, "Negative", engine.apply(-1));
	}

	private void checkPosAndBigPos(IEngine1<String, Integer> engine) {
		String engineString = engine.toString();
		assertEquals(engineString, "Positive", engine.apply(1));
		assertEquals(engineString, "BigPositive", engine.apply(100));
	}

}
