package org.autoTdd.schengen;

import org.autoTdd.jbehave.because.IBecause2;

public  final class BecauseMoney extends AbstractSchengenBecause{

	@Override
	public boolean evaluate(Schengen value1, Person value2) {
		return value2.money >= AccessSteps.aLotOfMoney;
	}
}