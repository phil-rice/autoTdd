package org.autotdd.engine
import org.autotdd.engine._
import org.autotdd.constraints._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EngineThreeConstraintTests extends EngineStringTests[String] {

  "An engine" should "allow any combination of three constraints to work" in {
    val becauseA = new Because[B]((h => h contains "A"), "hA");
    val becauseB = new Because[B]((h => h contains "B"), "hB");
    val becauseAB = new Because[B]((h => (h contains "A") & (h contains "B")), "hAB");
    val a = Scenario("A").produces("XA").becauseBecause(becauseA);
    val b = Scenario("B").produces("XB").becauseBecause(becauseB);
    val ab = Scenario("AB").produces("XAB").becauseBecause(becauseAB);
    Engine1[String, String]("Z", UseCase("", a, ab)).validateConstraints
    Engine1[String, String]("Z", UseCase("", ab, a)).validateConstraints
    Engine1[String, String]("Z", UseCase("", a, ab, b)).validateConstraints
    Engine1[String, String]("Z", UseCase("", a, b, ab)).validateConstraints
    Engine1[String, String]("Z", UseCase("", ab, a, b)).validateConstraints
    Engine1[String, String]("Z", UseCase("", ab, b, a)).validateConstraints
    Engine1[String, String]("Z", UseCase("", b, a, ab)).validateConstraints
    Engine1[String, String]("Z", UseCase("", b, ab, a)).validateConstraints
//    println("Engine: " + Engine1[String, String]("Z", UseCase("", a, ab)))
  }
}