package org.autotdd.engine

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

trait AbstractScenarioTests[R] extends FirstScenarioTest[R] {

  /** Makes a blank scenario with the params in 'params'. The use case has description 'description' */

  def codeFn: RFn
  def because: B
  def falseBecause: B
  def firstResult: R
  def checkRfn(c: Scenario): Unit
  def checkExpected(c: Scenario): Unit
  def build(builder: RealScenarioBuilder): Engine

  "A scenario" should " remember parameters and have None for the other parameters" in {
    val c = firstScenario
    assert(c.params == firstParams)
    assert(c.code == None)
    assert(c.because == None)
    assert(c.expected == ROrException[R]())
    assert(c.configuration == None)
    assert(c.description == None, c.description)
  }

  "The ScenarioBuilder" should "allow the scenario lens to set /get the scenario" in {
    val expected = new Scenario("", None, List(), logger)
    val newBuilder = scenarioLens.set(builderWithScenario, expected);
    assert(List(expected) == newBuilder.useCases.head.scenarios, newBuilder.useCases.head.scenarios) //i.e. this has replaced the head scenario
    assert(expected == scenarioLens.get(newBuilder), scenarioLens.get(newBuilder))
  }

  it should "add expected when produces is called" in {
    val c = scenarioLens.get(builderWithScenario.expected(firstResult))
    assert(c.params == firstParams)
    assert(c.code == None)
    assert(c.because == None)
    assert(c.expected == ROrException[R](firstResult), c)
    assert(c.configuration == None)
  }

  it should "add code when byCalling is called" in {
    val c = scenarioLens.get(builderWithScenario.expected(firstResult).code(codeFn))
    assert(c.params == firstParams)
    val expectedCode = Some(new CodeFn(codeFn, "AbstractScenarioTests.this.codeFn"))
    assert(c.code == expectedCode, "Scenario: " + c + "\nCode: " + c.code + "\nExpected: " + expectedCode)
    assert(c.because == None)
    assert(c.expected ==  ROrException[R](firstResult))
    assert(c.configuration == None)
  }

  it should "add because when because is called" in {
    val c = scenarioLens.get(builderWithScenario.expected(firstResult).because(because))
    assert(c.params == firstParams)
    assert(c.code == None)
    val expected = Some(new Because(because, "AbstractScenarioTests.this.because"))
    assert(c.because == expected, c.because)
    assert(c.expected == ROrException[R](firstResult))
    assert(c.configuration == None)
  }

  it should "use expected as actualCode if code isn't specified" in {
    val c = scenarioLens.get(builderWithScenario.expected(firstResult))
    checkExpected(c);
  }

  it should "use code as actualCode if specified" in {
    val c = scenarioLens.get(builderWithScenario.code(codeFn))
    checkRfn(c)
  }

  "A built engine" should "have all the scenarios in it's list of scenarios" in {
    val b = builderWithScenario.expected(firstResult)
    val engine = build(b)
    assertEquals("UseCases", b.useCasesForBuild, engine.useCases)
    val expectedScenario = Scenario(firstUseCaseDescription + "[0]", None, firstParams, logger, ROrException[R](firstResult));
    assertEquals(List(expectedScenario), engine.scenarios)
  }

  it should "allow a specified description for a use ca to be used" in {

  }

}

@RunWith(classOf[JUnitRunner])
class Scenario1Tests extends FirstScenario1Test[Int, Int] with AbstractScenarioTests[Int] {

  val firstParams: List[Any] = List(1)
  val codeFn = (i: Int) => i + 1
  val because = (i: Int) => true
  val falseBecause = (i: Int) => false
  def firstResult = 2
  def build(b: RealScenarioBuilder) = b.build

  def checkRfn(c: Scenario) = {
    assert(2 == c.actualCode.rfn(1))
    assert(0 == c.actualCode.rfn(-1))
  }
  def checkExpected(c: Scenario) = {
    assert(firstResult == c.actualCode.rfn(1))
    assert(firstResult == c.actualCode.rfn(-1))
  }

  it should "manipulate the second scenario when a new one is added" in {
    val b = builderWithScenario.expected(firstResult).scenario(3).expected(7).because((x: Int) => x > 2);
    val e = b.build;
    val firstScenaro = e.scenarios(0)
    val secondScenaro = e.scenarios(1)
    assertEquals(2, e.scenarios.size)

    assertEquals("", firstScenaro.becauseString)
    assertEquals(ROrException[Int](firstResult), firstScenaro.expected)

    assertEquals("((x: Int) => x.>(2))", secondScenaro.becauseString)
    assertEquals(ROrException[Int](7), secondScenaro.expected)
  }

  it should "have a tostring method that includes the locator and the descriptor if it exists" in {
    val b = builderWithUseCase.scenario(111).expected(123).scenario(222, "withDescription").because((x: Int) => x == 222).expected(234).build;
    val s0 = b.scenarios(0);
    val s1 = b.scenarios(1);
    assertEquals("Scenario(UseCase1[0], 111, because=, expected=123)", s0.toString)
    assertEquals("Scenario(withDescription, 222, because=((x: Int) => x.==(222)), expected=234)", s1.toString)
  }

}
@RunWith(classOf[JUnitRunner])
class Scenario2Tests extends FirstScenario2Test[Int, Int, Int] with AbstractScenarioTests[Int] {
  val firstParams: List[Any] = List(1, 1)
  val codeFn: RFn = (i: Int, j: Int) => i + j
  val because = (i: Int, j: Int) => true
  val falseBecause = (i: Int, j: Int) => false
  def firstResult = 2
  def build(b: RealScenarioBuilder) = b.build

  def checkRfn(c: Scenario) = {
    assert(2 == c.actualCode.rfn(1, 1))
    assert(0 == c.actualCode.rfn(-1, 1))
  }
  def checkExpected(c: Scenario) = {
    assert(firstResult == c.actualCode.rfn(1, 1))
    assert(firstResult == c.actualCode.rfn(-1, 1))
  }

}
