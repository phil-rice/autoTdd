package org.autoTdd;

import junit.framework.TestCase;

import org.autoTdd.engine.Constraint;
import org.autoTdd.engine.Engine;


public class EngineTest extends TestCase {

	static abstract class TestConstraint extends Constraint<Integer, Integer, String> {
		public TestConstraint(Integer input, String result) {
			super(input, result);
		}

		@Override
		public String result(Integer match) {
			return Integer.toString(match);
		}
	}

	TestConstraint threeMoreTwo = new TestConstraint(3, "222") {
		@Override
		public Integer match(Integer input) {
			if (input > 2)
				return 222;
			return null;
		}
	};
	TestConstraint fourMoreThree = new TestConstraint(4, "333") {
		@Override
		public Integer match(Integer input) {
			if (input > 3)
				return 333;
			return null;
		}
	};
	TestConstraint fiveMoreFour = new TestConstraint(5, "444") {
		@Override
		public Integer match(Integer input) {
			if (input > 4)
				return 444;
			return null;
		}
	};

	public void testBlankEngine() {
		Engine<String, String, String> engine = new Engine<String, String, String>("default");
		assertEquals("default", engine.defaultValue());
		assertEquals("default", engine.apply(null));
		assertEquals("default", engine.apply("any"));
	}

	@SuppressWarnings("unchecked")
	public void test1() {
		Engine<Integer, Integer, String> engine = new Engine<Integer, Integer, String>("default");
		engine.add(threeMoreTwo, fourMoreThree);
		assertEquals("default", engine.apply(1));
		assertEquals("default", engine.apply(2));
		assertEquals("222", engine.apply(3));
		assertEquals("333", engine.apply(4));
		assertEquals("333", engine.apply(5));
	}

	@SuppressWarnings("unchecked")
	public void test2() {
		Engine<Integer, Integer, String> engine = new Engine<Integer, Integer, String>("default");
		engine.add(fourMoreThree, threeMoreTwo);
		assertEquals("default", engine.apply(1));
		assertEquals("default", engine.apply(2));
		assertEquals("222", engine.apply(3));
		assertEquals("333", engine.apply(4));
		assertEquals("333", engine.apply(5));
	}
	@SuppressWarnings("unchecked")
	public void test3() {
		Engine<Integer, Integer, String> engine = new Engine<Integer, Integer, String>("default");
		engine.add(fourMoreThree, threeMoreTwo, fiveMoreFour);
		assertEquals("default", engine.apply(1));
		assertEquals("default", engine.apply(2));
		assertEquals("222", engine.apply(3));
		assertEquals("333", engine.apply(4));
		assertEquals("444", engine.apply(5));
		assertEquals("444", engine.apply(6));
	}

}
