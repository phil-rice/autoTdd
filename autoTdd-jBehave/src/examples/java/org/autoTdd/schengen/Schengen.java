package org.autoTdd.schengen;

public class Schengen {

	private boolean sarsState;

	public String assess(Person entrant) {
		boolean lotsOfMoney = entrant.money > SchengenConstants.lotsOfMoneyThreshold;

		if (lotsOfMoney)
			return SchengenConstants.detain;
		else if (sarsState)
			return SchengenConstants.deny;
		else
			return SchengenConstants.allow;
	}

	public void setSarsState(boolean sarsState) {
		this.sarsState = sarsState;

	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Schengen schengen = new Schengen();
		schengen.setSarsState(sarsState);
		return schengen;
	}

	@Override
	public String toString() {
		return "Schengen [sarsState=" + sarsState + "]";
	}

}
