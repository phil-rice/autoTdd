package org.autotdd.engine
import org.autotdd.engine._

import scala.runtime.ZippedTraversable2.zippedTraversable2ToTraversable
import org.scalatest.FlatSpec

trait EngineTests extends IfThenParserTestTrait with FlatSpec  {

  def check(engine: Engine1[String, String], expected: String) {
    val exceptedTree = p(expected)
    val actual = comparator.compare(exceptedTree, engine.root)
    assert(actual == List(), actual + "\nExpected: " + exceptedTree + "\n Actual: " + engine.root + "\nEngine:\n" + engine)
  }
  def checkConstraintsExist(engine: Engine1[String, String], expected: String*) {
    assert(engine.constraints.size == expected.size)
    for ((c, a) <- (engine.constraints, expected).zipped) {
      assert(c.becauseString == a, "Expected: [" + a + "] BecauseString = [" + c.becauseString + "] Actual " + c + "\n   Constraints: " + engine.constraints+"\nEngine:\n"+engine)
    }
  }
  def makeAndCheck(constraints: C*) = {
    val engine = Engine((p: String) => "Zero", constraints.toList);
    for (c <- constraints) {
      val p = c.params
      assert(c.expected == engine.applyParam(engine.root, p), "\nEngine:\n" + engine + "\nConstraint: " + c);
    }
  }

  def makeAndCheckToString(expected: String, constraints: C*) = {
    val engine = Engine((p: String) => "Zero", constraints.toList);
    assertToStringMatches(engine, expected);
  }

  def assertToStringMatches(engine: Engine1[String,String], expected: String) {
    val actual = engine.toString
    assert(expected == actual, "Expected\n[" + expected + "]\nActual:\n[" + actual + "]")
  }
}
