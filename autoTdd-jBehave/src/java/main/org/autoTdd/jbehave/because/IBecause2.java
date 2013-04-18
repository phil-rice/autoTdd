package org.autoTdd.jbehave.because;

public interface IBecause2<T1,T2> extends IBecause {

	boolean evaluate(T1 value1, T2 value2);
	
}
