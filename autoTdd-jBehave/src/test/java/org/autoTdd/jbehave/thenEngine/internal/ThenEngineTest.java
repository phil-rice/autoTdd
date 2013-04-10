package org.autoTdd.jbehave.thenEngine.internal;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.autoTdd.jbehave.thenEngine.IThenEngine;
import org.autoTdd.jbehave.thenEngine.IThenStrategy;
import org.easymock.EasyMock;
import org.jbehave.core.annotations.Then;
import org.softwarefm.utilities.callbacks.ICallback;

public class ThenEngineTest extends TestCase {

	@Then("not used")
	public void method() {

	}

	IThenStrategy thenInteger;
	IThenStrategy thenNumber;
	IThenStrategy thenString;
	IThenStrategy thenDouble;
	private Method method;
	private Then then;

	public void testFindsTheThenStrategyBasedOnOrderAndInheritance() throws Exception {
		checkThen(1, new ICallback<Object>() {
			@Override
			public void process(Object object) throws Exception {
				thenInteger.execute(then, method, object);
			}
		});
		checkThen("1", new ICallback<Object>() {
			@Override
			public void process(Object object) throws Exception {
				thenString.execute(then, method, object);
			}
		});
		checkThen(1.0d, new ICallback<Object>() {
			@Override
			public void process(Object object) throws Exception {
				thenNumber.execute(then, method, object);
			}
		});
	}
	
	public void testDefaultThenEngine(){
		IThenEngine thenEngine = IThenEngine.Utils.defaultThens();
		final AtomicInteger count = new AtomicInteger();
		thenEngine.execute(then, method, new Runnable() {
			@Override
			public void run() {
				count.incrementAndGet();
			}
		});
		assertEquals(1, count.get());
		
		thenEngine.execute(then, method, 1);
		thenEngine.execute(then, method, null);
		assertEquals(1, count.get());
		
	}

	private void checkThen(Object result, ICallback<Object> setup) throws Exception {
		create();
		setup.process(result);
		replay();
		IThenEngine engine = IThenEngine.Utils.builder().register(Integer.class, thenInteger).register(Number.class, thenNumber).register(String.class, thenString).register(Double.class, thenDouble).build();
		engine.execute(then, method, result);
		verify();
	}

	private void create() {
		thenInteger = EasyMock.createMock(IThenStrategy.class);
		thenNumber = EasyMock.createMock(IThenStrategy.class);
		thenString = EasyMock.createMock(IThenStrategy.class);
		thenDouble = EasyMock.createMock(IThenStrategy.class);

	}

	private void replay() {
		EasyMock.replay(thenDouble, thenInteger, thenNumber, thenString);
	}

	private void verify() {
		EasyMock.verify(thenDouble, thenInteger, thenNumber, thenString);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		method = getClass().getMethod("method");
		then = method.getAnnotation(Then.class);
	}
}
