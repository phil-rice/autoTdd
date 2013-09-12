package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EngineSecondScenarioTests extends EngineStringStringTests {

  override val logger = new ConsoleLogger

  "An  engine" should "Add assertions to the no if scenario comes to correct value" in {
    val bldr = builderWithDefault.
      scenario("A").because("A").expected("X").
      scenario("B").expected("Z");
    val e = bldr.build

    val w = e.scenarios(0); assertEquals("", w.becauseString)
    val a = e.scenarios(1); assertEquals("A", a.becauseString)
    val b = e.scenarios(2); assertEquals("", b.becauseString)

    assertEngineMatches(e, Right(Node(because = "A", scenarioThatCausedNode = a, inputs = List("A"),
      yes = Left(CodeAndScenarios("X", List(a))),
      no = Left(CodeAndScenarios("Z", List(b, w))))))
  }

  it should "Add assertions to the yes if scenario comes to correct value" in {
    val b = builderWithDefault.
      scenario("A").because("A").expected("X").
      scenario("AB").expected("X")

    val e = b.build
    val u1 = b.useCases
    val u2 = b.useCasesForBuild
    val s1 = e.scenarios
    val w = e.scenarios(0); assertEquals(List("W"), w.params)
    val a = e.scenarios(1); assertEquals(List("A"), a.params)
    val ab = e.scenarios(2); assertEquals(List("AB"), ab.params)

    assertEngineMatches(e, Right(Node(because = "A", inputs = List("A"), yes = Left(CodeAndScenarios("X", List(ab, a))), no = Left(CodeAndScenarios("Z", List(w))), scenarioThatCausedNode = a)))
  }

  //TODO Come back and decide what the correct behaviour is here
  //  it should "add to no clause if because is false for root" in {
  //    val e = builderWithDefault.
  //      scenario("A").because("A").expected("X").
  //      scenario("B").because("B").expected("Y").
  //      build
  //
  //    val w = e.scenarios(0); assertEquals(List("W"), w.params)
  //    val a = e.scenarios(1); assertEquals(List("A"), a.params)
  //    val b = e.scenarios(2); assertEquals(List("B"), b.params)
  //
  //    assertEngineMatches(e,
  //      Right(Node(because = "A", scenarioThatCausedNode = a, inputs = List("A"),
  //        yes = Left(CodeAndScenarios("X": Code, List(a))),
  //        no = Right(Node(because = "B", scenarioThatCausedNode = b, inputs = List("B"),
  //          yes = Left(CodeAndScenarios("Y", List(b))),
  //          no = Left(CodeAndScenarios("Z", List(w))))))))
  //  }

  it should "add to yes clause if because is true for root" in {
    val e = builderWithDefault.
      scenario("A").because("A").expected("X").
      scenario("AB").because("B").expected("Y").
      build

    val w = e.scenarios(0); assertEquals(List("W"), w.params)
    val a = e.scenarios(1); assertEquals(List("A"), a.params)
    val ab = e.scenarios(2); assertEquals(List("AB"), ab.params)

    assertEngineMatches(e,
      Right(Node(because = "A", scenarioThatCausedNode = a, inputs = List("A"),
        yes = Right(Node(because = "B", scenarioThatCausedNode = ab, inputs = List("AB"),
          yes = Left(CodeAndScenarios("Y", List(ab))),
          no = Left(CodeAndScenarios("X", List(a))))),
        no = Left(CodeAndScenarios("Z", List(w))))))
  }

  //TODO Consider how to deal with identical result, different because. It's not clear to me what I should do
  it should "throw ScenarioConflictException if  cannot differentiate inputs, identical result, different because" in {
    val bldr = builderWithDefault.
      scenario("AB").because("A").expected("X").
      scenario("AB").because("B").expected("X");

    val s = bldr.useCasesForBuild.flatMap(_.scenarios)
    val w = s(0); assertEquals(List("W"), w.params)
    val xBecauseA = s(1); assertEquals("A", xBecauseA.becauseString)
    val xBecauseB = s(2); assertEquals("B", xBecauseB.becauseString)
    val e = evaluating { bldr.build } should produce[ScenarioConflictException]
  }

  it should "throw ScenarioConflictException if it cannot decide between two scenarios" in {
    val bldr = builderWithDefault.
      scenario("AB").because("A").expected("X").
      scenario("AB").because("B").expected("Y");

    val e = evaluating { bldr.build } should produce[ScenarioConflictException]

  }

  it should "Throw ScenarioConflictingWithDefaultException if scenario matches root condition and comes to wrong conclusion" in {
    val bldr = builderWithDefault.
      scenario("AB").expected("X")
    evaluating {
      bldr.build
    } should produce[ScenarioConflictingWithDefaultException]

  }

  //  it should "Throw ScenarioConflictingWithDefaultException if scenario doesnt match root condition and comes to wrong conclusion" in {
  //    val bldr = builderWithDefault.
  //      scenario("A").because("A").expected("X"). //
  //      scenario("B").expected("Y"); //no because so should have come to 
  //
  //    evaluating { bldr.build } should produce[ScenarioConflictingWithDefaultException]
  //  }

  it should "throw ScenarioConflictingWithoutBecause if no because and doesnt come to correct result" in {
    val b = builderWithDefault.
      scenario("A").because("A").expected("X").
      scenario("AB").expected("Y")
    evaluating { b.build } should produce[ScenarioConflictingWithoutBecauseException]
  }
  
 
}