package org.autoTdd.jbehaveEclipse.steps;

import junit.framework.Assert;

import org.autoTdd.jbehaveEclipse.GarmentType;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.softwarefm.schengen.IEngineSteps;

public class GarmentSteps implements IEngineSteps {

	private GarmentType garmentType;

	@Given("a garment called $nameAndColour")
	public void aGarmentCalled(String nameAndColour) {
		garmentType = new GarmentType(nameAndColour);
	}

	@Then("I should have a garment type with the colour $colour and the name $name")
	public void iShouldHaveAGarmentTypeWithColourAndName(String colour, String name) {
		Assert.assertEquals(colour, garmentType.colour);
		Assert.assertEquals(name, garmentType.name);
	} 

	@Override
	public Object[] getInputs() {
		return new Object[] { garmentType };
	}

}
