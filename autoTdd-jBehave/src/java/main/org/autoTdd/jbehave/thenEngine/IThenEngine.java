package org.autoTdd.jbehave.thenEngine;

import java.lang.reflect.Method;

import org.autoTdd.jbehave.thenEngine.internal.ThenEngineBuilder;
import org.jbehave.core.annotations.Then;

public interface IThenEngine extends IThenStrategy {

	public static class Utils {
		public static IThenEngineBuilder builder() {
			return new ThenEngineBuilder();
		}

		public static IThenStrategy runnableThenStrategy() {
			return new IThenStrategy() {
				@Override
				public void execute(Then then, Method method, Object result) {
					((Runnable) result).run();
				}
			};
		}
		
		public static IThenStrategy noThenStrategy(){
			return new IThenStrategy() {
				@Override
				public void execute(Then then, Method method, Object result) {
				}
			};
		}

		public static IThenEngine defaultThens() {
			return new ThenEngineBuilder().//
					register(Runnable.class, runnableThenStrategy()).//
					register(Void.class, noThenStrategy()).//
					register(Object.class,noThenStrategy()).build();
		}
	}

}
