package org.autoTdd;

import org.autoTdd.groovy.DSL;
import org.autoTdd.internal.Constraint;

public class ConstraintsForTests {
	public final static Constraint pos = DSL.constraint("Positive", "x>0", 1);
	public final static Constraint pos2 = DSL.constraint("Positive2", "x>0", 1);
	public final static Constraint bigPos = DSL.constraint("BigPositive", "x>10", 20);
	public final static Constraint vBigPos = DSL.constraint("VBigPositive", "x>1000", 2000);
	public final static Constraint neg = DSL.constraint("Negative", "x<0", -1);
	public final static Constraint bigNeg = DSL.constraint("BigNegative", "x<-10", -20);
	public final static Constraint vBigNeg = DSL.constraint("VBigNegative", "x<-1000", -2000);


}
