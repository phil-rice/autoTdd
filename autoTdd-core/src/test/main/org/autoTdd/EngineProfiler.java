package org.autoTdd;

import static org.autoTdd.EngineTest.bigNeg;
import static org.autoTdd.EngineTest.bigPos;
import static org.autoTdd.EngineTest.neg;
import static org.autoTdd.EngineTest.pos;
import static org.autoTdd.EngineTest.vBigNeg;
import static org.autoTdd.EngineTest.vBigPos;

import org.autoTdd.engine.IEngine1;
import org.autoTdd.groovy.DSL;

public class EngineProfiler {
	public static int warmup = 10000;
	public static int times = 10000;

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		IEngine1<String, Integer> engine = DSL.simple(String.class, Integer.class, "Zero", "x").//
				add(pos, bigPos, neg, bigNeg, vBigNeg, vBigPos).engine1(String.class, Integer.class);
		String engineString = engine.toString();
		for (int i = 0; i < warmup; i++) {
			engine.apply(0);
			engine.apply(-5);
			engine.apply(5);
			engine.apply(-50);
			engine.apply(50);
			engine.apply(-500);
			engine.apply(500);
		}
		System.out.println(engine);
		System.out.println("Warmed up");
		time(engine, 0, 5, 50, 500, -5, -50, -500);
		time(engine, 0, 5, 50, 500, -5, -50, -500);
		// timeMakingShell(engine);
		// timeEvaluating();

	}

	private static void time(IEngine1<String, Integer> engine, int... times) {
		for (int value : times)
			timeOne(engine, value);

	}

	private static void timeOne(IEngine1<String, Integer> engine, int value) {
		long start = System.nanoTime();
		for (int i = 0; i < times; i++)
			engine.apply(value);
		long durationNs = System.nanoTime() - start;
		System.out.println(String.format("Time for %5d took: %,7.3fus %s", value, durationNs / 1000.0 / times, engine.apply(value)));
	}
}
