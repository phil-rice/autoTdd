 package org.autoTdd.schengen;

import org.autoTdd.jbehave.annotations.Because;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.softwarefm.schengen.IEngineSteps;

public class DetainSteps implements IEngineSteps {

	@Override
	public Object[] getInputs() {
		return new Object[0];
	}

	@When("I decide who to detain")
	public void whenIDecideWhoToDetail() {

	}

	@Then("I select a policeman")
	public void thenISelectAPoliceman() {
	}

	@When("the policeman decides what to do")
	public void whenThePolicemanDecidesWhatToDo() {
	}

	@Then("He lets him through")
	public void thenHeLetsHimThrough() {
	}

	@Because("Francisco has a letter of immunity")
	public void becauseFranciscoHasALetterOfImmunity() {
	}
}
