package org.autoTdd.jbehaveEclipse.steps;

import junit.framework.Assert;

import org.autoTdd.jbehave.because.IBecause1;
import org.autoTdd.jbehaveEclipse.GarmentType;
import org.autoTdd.jbehaveEclipse.Stock;
import org.jbehave.core.annotations.Because;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.softwarefm.schengen.IEngineSteps;

public class StockSteps implements IEngineSteps {

	private Stock stock;
	private GarmentType customersGarmentType;

	@BeforeScenario
	public void preScenario() {
		stock = new Stock();
		customersGarmentType = null;
	}

	@Given("a customer previously bought $count of $nameAndColour from me")
	public void aCustomerPreviouslyBoughtFromMe(int count, String nameAndColour) {
		customersGarmentType = new GarmentType(nameAndColour);
	}

	@Given("that a customer buys $count of $nameAndColour")
	public void aCustomerBuysCountItems(String count, String nameAndColour) {
		customersGarmentType = new GarmentType(nameAndColour);
	}

	@Given("{I currently have|I have} $count of $items in stock")
	public void iCurrentlyHaveCountItemsLeftInstock(int count, String item) {
		stock.setStockForTest(count, item);
	}

	@When("he returns the $item for a refund")
	public void heReturnsTheItemForARefund(String notUsedItem) {
	}

	@When("he returns the $dummyItem for a replacement in $newColor")
	public void heReturnsTheItemForAReplacementIn(String notUsedItem,
			String newColor) {
	}

	@Then("the $item should be returned to stock")
	/** Comment about the item being returned to stock */
	public Runnable returnItemToStock(String item) {
		Assert.assertNotNull(customersGarmentType);
		return new Runnable() {
			public void run() {
				stock.addToStockLevel(1, customersGarmentType);
			}
		};
	} 

	@Then("The customer should be given a $item")
	public Runnable customerGivenItem(final String item) {
		Assert.assertNotNull(customersGarmentType);
		return new Runnable() {
			public void run() {
				stock.addToStockLevel(-1, new GarmentType(item));
			}
		};
	}

	@Then("I should have $count of $item in stock")
	public void iShouldHaveCountItemsInStock(int count, String item) {
		int result = stock.getStockLevel(new GarmentType(item));
		Assert.assertEquals(stock.toString(), count, result);
	}

	@Because("that's the default")
	public IBecause1<Stock> thatsTheDefault() {
		return new IBecause1<Stock>() {
			@Override
			public boolean evaluate(Stock t) {
				return true;
			}
		};
	}

	@Override
	public Object[] getInputs() {
		return new Object[] { stock };
	}

}
