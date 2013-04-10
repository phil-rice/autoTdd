package org.autoTdd.jbehaveEclipse.steps;

import junit.framework.Assert;

import org.autoTdd.jbehaveEclipse.GarmentType;
import org.autoTdd.jbehaveEclipse.Stock;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.softwarefm.schengen.IEngineSteps;

public class StockSteps implements IEngineSteps {

	private Stock stock;

	@BeforeScenario
	public void preScenario() {
		stock = new Stock();
	}

	@Given("a customer previously bought $count of $item from me")
	public void aCustomerPreviouslyBoughtFromMe(int count, String item) {
		// not actually interested in this at the moment
	}

	@Given("that a customer buys $count of $item")
	public void aCustomerBuysCountItems(String count, String item) {
		// not actually interested in this at the moment
	}

	@Given("{I currently have|I have} $count of $items {left|} in stock")
	public void iCurrentlyHaveCountItemsLeftInstock(int count, String item) {
		stock.setStockForTest(count, item);
	}

	@When("he returns the $item for a refund")
	public void heReturnsTheItemForARefund(String notUsedItem) {
	}

	@When("he returns the $dummyItem for a replacement in $newColor")
	public void heReturnsTheItemForAReplacementIn(String notUsedItem, String newColor) {
	}

	@Then("I should have $count $item in stock")
	public int iShouldHaveCountItemsInStock(int count, String item) {
		int result = stock.getStockLevel(new GarmentType(item));
		return result;
	}

	@Override
	public Object[] getInputs() {
		return null;
	}

}
