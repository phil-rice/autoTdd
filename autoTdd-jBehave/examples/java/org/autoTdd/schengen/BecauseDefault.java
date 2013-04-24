package org.autoTdd.schengen;


public  final class BecauseDefault extends AbstractSchengenBecause  {
	@Override
	public boolean evaluate(Schengen value1, Person value2) {
		return true;
	}
}