package org.autotdd.engine
import org.autotdd.engine._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EngineThreeScenarioTests extends EngineStringTests[String] {

  "An engine" should "allow any combination of three scenarios to work" in {
    val becauseA = new Because[B]((h => h contains "A"), "hA");
    val becauseB = new Because[B]((h => h contains "B"), "hB");
    val becauseAB = new Because[B]((h => (h contains "A") & (h contains "B")), "hAB");
    val a = Scenario("A").produces("XA").becauseBecause(becauseA);
    val b = Scenario("B").produces("XB").becauseBecause(becauseB);
    val ab = Scenario("AB").produces("XAB").becauseBecause(becauseAB);
    Engine1[String, String]("Z", UseCase("", a, ab)).validateScenarios
    Engine1[String, String]("Z", UseCase("", ab, a)).validateScenarios
    Engine1[String, String]("Z", UseCase("", a, ab, b)).validateScenarios
    Engine1[String, String]("Z", UseCase("", a, b, ab)).validateScenarios
    Engine1[String, String]("Z", UseCase("", ab, a, b)).validateScenarios
    Engine1[String, String]("Z", UseCase("", ab, b, a)).validateScenarios
    Engine1[String, String]("Z", UseCase("", b, a, ab)).validateScenarios
    Engine1[String, String]("Z", UseCase("", b, ab, a)).validateScenarios
//    println("Engine: " + Engine1[String, String]("Z", UseCase("", a, ab)))
  }
}