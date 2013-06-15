package org.autotdd.engine

import org.autotdd.engine._
import org.autotdd.constraints._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class EngineConstructionStringTest extends EngineStringStringTests {

  val becauseA = new Because[B]((h => h contains "A"), "hA");
  val becauseB = new Because[B]((h => h contains "B"), "hB");
  val becauseAB = new Because[B]((h => (h contains "A") & (h contains "B")), "hAB");
  val aS = Scenario("A").produces("XA").becauseBecause(becauseA);
  val bS = Scenario("B").produces("XB").becauseBecause(becauseB);
  val abS = Scenario("AB").produces("XAB").becauseBecause(becauseAB);
  val useCase = UseCase("", aS, bS, abS)
  val engine = Engine1[String, String]("Z", useCase);

  val aCString = "if(hA)\n" +
    " XA:[0]\n" +
    "else\n" +
    " Z:\n";
  val b_aCString = "if(hA)\n" +
    " XA:[0]\n" +
    "else\n" +
    " if(hB)\n" +
    "  XB:[1]\n" +
    " else\n" +
    "  Z:\n"
  val ab_b_aCString = "if(hA)\n" +
    " if(hAB)\n" +
    "  XAB:[2]\n" +
    " else\n" +
    "  XA:[0]\n" +
    "else\n" +
    " if(hB)\n" +
    "  XB:[1]\n" +
    " else\n" +
    "  Z:\n"

  "An engine's construction string" should "be included in constraint conflict exceptions to help explain how the exception happened" in {
    val xBecauseA = Scenario("AB").becauseBecause("A").produces("X")
    val ybecauseA = Scenario("AB").becauseBecause("A").produces("Y")
    val e = evaluating { Engine1[String, String]("Z", UseCase("", xBecauseA, ybecauseA)) } should produce[ConstraintConflictException]
    assert(e.getMessage().contains(Engine1[String, String]("Z", UseCase("", xBecauseA)).constructionString), "Message: " + e.getMessage())
  }

  it should "be generated even if exceptions occur. The exceptions should be included" in {
    val xBecauseA = Scenario("AB").becauseBecause(becauseA).produces("XA")
    val yBecauseA = Scenario("AB").becauseBecause(becauseA).produces("YA")
    val e = evaluating { Engine1[String, String]("Z", UseCase("", xBecauseA, yBecauseA)) } should produce[ConstraintConflictException]
    val useCase = UseCase("", xBecauseA, yBecauseA) //needed to get the if / then string correct
    val actual = engine.constructionString(Left("Z"), List(xBecauseA, yBecauseA).map(_.withScenarioHolder(useCase)));
    val expected = aCString + "\n" + e.getClass() + "\n" + e.getMessage()
    assert(expected == actual, "Expected: " + expected + "\nActual: " + actual)

  }

  it should "return the aggregate of the toString of an engine created from the constraints, one after another" in {
    assert(aCString + "\n" + b_aCString + "\n" + ab_b_aCString == engine.constructionString, engine.constructionString)
  }

  "An engine's increasingConstraintsList method" should "return a list of increasing numbers of constraints" in {
    val actual = engine.increasingConstraintsList(engine.constraints)
    val expected = List(List(abS, bS, aS), List(bS, aS), List(aS)).map(_.map(_.withScenarioHolder(useCase)))
    assert(expected == actual, "Actual:  " + actual + "\nExpected: " + expected)
  }

  "An engine's buildRoot method" should "return a node that represents the constraints" in {
    val actual = engine.buildRoot(engine.defaultRoot, List(aS, bS, abS).map(_.withScenarioHolder(useCase)))
    assertEngineMatches(engine, actual)
  }

}