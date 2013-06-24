package org.autotdd.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.autotdd.engine._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class EngineSecondScenarioTests extends EngineStringStringTests {
  it should "Add assertions to the no if scenario comes to correct value" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val b = Scenario("B").produces("Z")
    val engine = Engine1[String, String]("Z", UseCase("", a, b))
    assertEngineMatches(engine, Right(Node(because = "A", scenarioThatCausedNode = a, inputs = List("A"), yes = Left(CodeAndScenarios("X",List(a))), no = Left(CodeAndScenarios("Z",List(b))))))
  }

  it should "Add assertions to the yes if scenario comes to correct value" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val ab = Scenario("AB").produces("X")
    val engine = Engine1[String, String](default = "Z", UseCase("", a, ab))
    assertEngineMatches(engine, Right(Node(because = "A", inputs = List("A"), yes = Left(CodeAndScenarios("X",  List(ab, a))), no = Left(CodeAndScenarios("Z")), scenarioThatCausedNode = a)))

  }

  it should "add to no clause if because is false for root" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val b = Scenario("B").becauseBecause("B").produces("Y")
    val engine = Engine1[String, String]("Z", UseCase("", a, b));
    assertEngineMatches(engine,
      Right(Node(because = "A", scenarioThatCausedNode = a, inputs = List("A"),
        yes = Left(CodeAndScenarios("X": Code,  List(a))),
        no = Right(Node(because = "B", scenarioThatCausedNode = b, inputs = List("B"),
          yes = Left(CodeAndScenarios("Y", List(b))),
          no = Left(CodeAndScenarios("Z")))))))
    checkScenariosExist(engine, "A", "B");
  }

  "An  engine" should "add to yes clause if because is true for root" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val ab = Scenario("AB").becauseBecause("B").produces("Y")
    val engine = Engine1[String, String]("Z", UseCase("", a, ab));
    assertEngineMatches(engine,
      Right(Node(because = "A", scenarioThatCausedNode = a, inputs = List("A"),
        yes = Right(Node(because = "B", scenarioThatCausedNode = ab, inputs = List("AB"),
          yes = Left(CodeAndScenarios("Y", List(ab))),
          no = Left(CodeAndScenarios("X", List(a))))),
        no = Left(CodeAndScenarios("Z")))))
    checkScenariosExist(engine, "A", "B");
  }

  //TODO Consider how to deal with identical result, different because. It's not clear to me what I should do
  it should "throw ScenarioConflictException if  cannot differentiate inputs, identical result, different because" in {
    val xBecauseA = Scenario("AB").becauseBecause("A").produces("X")
    val xbecauseB = Scenario("AB").becauseBecause("B").produces("X")
    val e = evaluating { Engine1[String, String]("Z", UseCase("", xBecauseA, xbecauseB)) } should produce[ScenarioConflictException]
  }

  it should "throw ScenarioConflictException if it cannot decide between two scenarios" in {
    val xBecauseA = Scenario("AB").becauseBecause("A").produces("X")
    val ybecauseA = Scenario("AB").becauseBecause("A").produces("Y")
    val e = evaluating { Engine1[String, String]("Z", UseCase("", xBecauseA, ybecauseA)) } should produce[ScenarioConflictException]

  }

  it should "Throw AssertionException if scenario matches root condition and comes to wrong conclusion" in {
    val a = Scenario("AB").becauseBecause("A").produces("X")
    val comesToDifferentConclusionWithoutBecause = Scenario[String, String]("AB").produces("Z")
    evaluating {
      Engine1[String, String]("Z", UseCase("", a, comesToDifferentConclusionWithoutBecause))
    } should produce[AssertionException]

  }
  it should "Throw AssertionException if scenario doesnt match root condition and comes to wrong conclusion" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val b = Scenario("B").produces("X") //should really come to Z as doesn't have because
    evaluating { Engine1[String, String]("Z", UseCase("", a, b)) } should produce[AssertionException]
  }

}