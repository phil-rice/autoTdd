package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ExceptionMessageTests extends EngineStringStringTests {

  "An engine" should "Throw ScenarioConflictingWithDefaultException if second scenario is assertion and comes to wrong result" in {
    val b = builderWithDefault.scenario("A").expected("X");
    val e = evaluating { b.build } should produce[ScenarioConflictingWithDefaultException]
    assertEquals("A", e.scenario.params(0))
    //    assertEquals(b.scenariosForBuild(0), e.scenario)
    assertEquals(b.scenariosForBuild(1), e.scenario)
    assertEquals("\nActual Result: Z\n" +
      "Expected X\n" +
      " Scenario Scenario(Some(UseCase1[1]),List(A),Some(X),None,None,List(),None)\n" +
      "Detailed:\n" +
      "  A", e.getMessage())
  }

  it should "Throw NoExpectedException if scenario doesnt have expected" in {
    val b = builderWithScenario.code("Z")
    val e = evaluating { b.build } should produce[NoExpectedException]
    assertEquals(b.scenariosForBuild(0), e.scenario)
    assertEquals("No 'produces' in Scenario(Some(UseCase1[0]),List(W),None,Some(CodeFn(Z)),None,List(),None)\n" +
      "Detailed:\n" +
      "  W", e.getMessage())

  }

  it should "Throw ScenarioBecauseException if because is not true in scenario" in {
    val b = builderWithDefault.scenario("B").because("X")
    val e = evaluating { b.build } should produce[ScenarioBecauseException]
    assertEquals(b.scenariosForBuild(1), e.scenario)
    assertEquals("X is not true for Scenario(Some(UseCase1[1]),List(B),None,None,Some(Because(X)),List(),None)\n" +
      "Detailed:\n" +
      "  B", e.getMessage())
  }
  it should "throw ScenarioConflictException if  cannot differentiate inputs, identical result, different because" in {
    val bldr = builderWithDefault.
      scenario("AB").because("A").expected("X").
      scenario("AB").because("B").expected("X");

    val s = bldr.useCasesForBuild.flatMap(_.scenarios)
    val w = s(0); assertEquals(List("W"), w.params)
    val xBecauseA = s(1); assertEquals("A", xBecauseA.becauseString)
    val xBecauseB = s(2); assertEquals("B", xBecauseB.becauseString)
    val e = evaluating { bldr.build } should produce[ScenarioConflictException]
    assertEquals("Cannot differentiate between\n" +
      "Existing: List(AB)\n" +
      "Being Added: List(AB)\n" +
      "\n" +
      "Details of Existing Scenario: Scenario(Some(UseCase1[1]),List(AB),Some(X),None,Some(Because(A)),List(),None)\n" +
      "Detailed:\n" +
      "  AB\n" +
      "\n" +
      "Details of New Scenario: Scenario(Some(UseCase1[2]),List(AB),Some(X),None,Some(Because(B)),List(),None)\n" +
      "Detailed:\n" +
      "  AB", e.getMessage())
  }

  it should "throw ScenarioConflictException if it cannot decide between two scenarios" in {
    val bldr = builderWithDefault.
      scenario("AB").because("A").expected("X").
      scenario("AB").because("B").expected("Y");

    val e = evaluating { bldr.build } should produce[ScenarioConflictException]
    assertEquals("Cannot differentiate between\n" +
      "Existing: List(AB)\n" +
      "Being Added: List(AB)\n" +
      "\n" +
      "Details of Existing Scenario: Scenario(Some(UseCase1[1]),List(AB),Some(X),None,Some(Because(A)),List(),None)\n" +
      "Detailed:\n" +
      "  AB\n" +
      "\n" +
      "Details of New Scenario: Scenario(Some(UseCase1[2]),List(AB),Some(Y),None,Some(Because(B)),List(),None)\n" +
      "Detailed:\n" +
      "  AB", e.getMessage())
  }

  it should "Throw ScenarioConflictingWithDefaultException if scenario matches root condition and comes to wrong conclusion" in {
    val bldr = builderWithDefault.
      scenario("AB").expected("X")

    val e = evaluating { bldr.build } should produce[ScenarioConflictingWithDefaultException]
    assertEquals(e.scenario, bldr.scenariosForBuild(1))
    assertEquals("\n" +
      "Actual Result: Z\n" +
      "Expected X\n" +
      " Scenario Scenario(Some(UseCase1[1]),List(AB),Some(X),None,None,List(),None)\n" +
      "Detailed:\n" +
      "  AB", e.getMessage())
  }
  it should "Throw ScenarioConflictingWithDefaultException if scenario doesnt match root condition and comes to wrong conclusion" in {
    val bldr = builderWithDefault.
      scenario("A").because("A").expected("X"). //
      scenario("B").expected("Y"); //no because so should have come to 

    val e = evaluating { bldr.build } should produce[ScenarioConflictingWithDefaultException]
    assertEquals(e.scenario, bldr.scenariosForBuild(2))
    assertEquals("\n" +
      "Actual Result: Z\n" +
      "Expected Y\n" +
      " Scenario Scenario(Some(UseCase1[2]),List(B),Some(Y),None,None,List(),None)\n" +
      "Detailed:\n" +
      "  B", e.getMessage())
  }
}