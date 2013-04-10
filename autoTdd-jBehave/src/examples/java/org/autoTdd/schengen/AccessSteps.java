package org.autoTdd.schengen;

import org.autoTdd.jbehave.IBecause2;
import org.autoTdd.jbehave.annotations.Because;
import org.autoTdd.jbehave.annotations.Called;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.softwarefm.schengen.IEngineSteps;

public class AccessSteps implements IEngineSteps {

	int aLotOfMoney = 10000;
	private Schengen schengen;
	private Person person;

	@BeforeScenario
	public void beforeScenario() {
		schengen = new Schengen();
	}

	@Given("a normal person")
	public void aNormalperson() {
		person = new Person();
	}

	@Given("a person with a lot of money")
	public void anpersonWithALotofMoney() {
		aNormalperson();
		person.setMoney(aLotOfMoney);
	}

	@Called("$name")
	public void called(String name) {
		person.setName(name);
	}

	@Because("I said so")
	public IBecause2<Schengen, Person> BecauseThatsTheDefault() {
		return new IBecause2<Schengen, Person>() {
			@Override
			public boolean evaluate(Schengen server, Person value) {
				return true;
			}
		};
	}

	@Because("the person has a lot of money")
	public IBecause2<Schengen, Person> becauseThePersonHasALotOfMoney() {
		return new IBecause2<Schengen, Person>() {
			@Override
			public boolean evaluate(Schengen server, Person value) {
				return value.money >= aLotOfMoney;
			}
		};
	}

	@When("I assess the person")
	public void whenIAssessThePerson() {
	}

	@Then("the person should be $schengenResult")
	public String thepersonShouldBe(String schengenResult) {
		return schengenResult;
	}

	@Override
	public Object[] getInputs() {
		return new Object[]{schengen, person};
	}

}
