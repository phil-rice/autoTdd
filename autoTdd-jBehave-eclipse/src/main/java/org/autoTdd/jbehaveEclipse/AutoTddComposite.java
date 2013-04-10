package org.autoTdd.jbehaveEclipse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.softwarefm.core.swt.HasComposite;
import org.softwarefm.core.swt.Swts;
import org.softwarefm.utilities.functions.IFunction1;

public class AutoTddComposite extends HasComposite{

	public AutoTddComposite(Composite parent) {
		super(parent);
	}

	public static void main(String[] args) {
		Swts.Show.display("Auto Tdd", new IFunction1<Composite, Composite>(){
			@Override
			public Composite apply(Composite arg0) throws Exception {
				AutoTddComposite autoTddComposite = new AutoTddComposite(arg0);
				return autoTddComposite.getComposite();
			}});
	}
	
}
