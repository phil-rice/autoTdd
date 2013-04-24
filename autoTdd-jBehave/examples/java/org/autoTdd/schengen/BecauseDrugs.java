package org.autoTdd.schengen;

import org.autoTdd.jbehave.because.IBecause2;

public  class BecauseDrugs extends AbstractSchengenBecause {
	@Override
	public boolean evaluate(Schengen value1, Person value2) {
		return value2.carryingDrugs ;
	}
}