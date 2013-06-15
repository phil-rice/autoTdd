package org.autotdd.engine

import org.autotdd.constraints._
import org.autotdd.engine._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class EngineNodeTests extends EngineStringStringTests {
  val becauseA = new Because[B]((h => h contains "A"), "hA");
  val becauseB = new Because[B]((h => h contains "B"), "hB");
  val becauseAB = new Because[B]((h => (h contains "A") & (h contains "B")), "hAB");
  val aS = Scenario("A").produces("XA").becauseBecause(becauseA);
  val bS = Scenario("B").produces("XB").becauseBecause(becauseB);
  val abS = Scenario("AB").produces("XAB").becauseBecause(becauseAB);

  "The root node of an engine" should "have a constraints method that lists all the constraints" in {
    checkEngine(aS);
    checkEngine(aS, bS);
    checkEngine(aS, bS, abS);
    checkEngine(abS, bS, aS);
  }

  def getEngine(c: C*) =
    Engine1[String, String]("Z", UseCase("", c: _*))

  def checkEngine(cs: C*) {
    val e = getEngine(cs: _*)
    val actual = e.root.right.get.allConstraints.toSet
    assert(actual == e.constraints.toSet, "Expected: " + e.constraints + "\nActual: " + actual)
  }

}