package org.autotdd.engine
import org.autotdd.engine._
import org.autotdd.engine.tests._
import scala.runtime.ZippedTraversable2.zippedTraversable2ToTraversable
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.language.implicitConversions
trait EngineTests[R] extends ShouldMatchers with FlatSpec with EngineTypes[R] {
  implicit def string_to_scenario(s: String) = Scenario(s).produces(s).because((x) => x contains s)

  def node(c: C, inputs: List[Any], yes: RorN, no: RorN) = new Node(c.because.get, inputs, yes, no, c);
  def rightNode(c: C, inputs: List[Any], yes: RorN, no: RorN) = Right(new Node(c.because.get, inputs, yes, no, c));

  def checkScenariosExist[X](engine: Engine1[X, R], expected: String*) {
    assert(engine.scenarios.size == expected.size)
    for ((c, a) <- (engine.scenarios, expected).zipped) {
      assert(c.becauseString == a, "Expected: [" + a + "] BecauseString = [" + c.becauseString + "] Actual " + c + "\n   Scenarios: " + engine.scenarios + "\nEngine:\n" + engine)
    }
  }

  def assertToStringMatches(engine: Engine1[String, String], expected: String) {
    val actual = engine.toString
    assert(expected == actual, "Expected\n[" + expected + "]\nActual:\n[" + actual + "]")
  }
  def checkMessages[X](engine: Engine[R], expected: String*) {
    val actual = engine.logger.asInstanceOf[TestLogger].messages
    assert(expected.toList == actual, "\nExpected: " + expected + "\nActual: " + actual)
  }
  def checkLastMessages(engine: Engine[R], expected: String*) {
    val full = engine.logger.asInstanceOf[TestLogger].messages
    val actual = full.takeRight(expected.size)
    assert(expected.toList == actual, "\nExpected: " + expected + "\nActual: " + full)
  }
}
trait EngineStringTests[X] extends EngineTests[String] with Engine1Types[X, String] {

  def comparator = NodeComparator.comparator1[X, String]
  def assertEngineMatches(e: Engine[String], n2: RorN) {
    val actual = comparator.compare(e.root.asInstanceOf[RorN], n2)
    assert(actual == List(), actual + "\n" + e.root)
  }

  def assertMatches(n1: RorN, n2: RorN) {
    val actual = comparator.compare(n1, n2)
    assert(actual == List(), actual)
  }
  implicit def string_to_rfn(s: String) = (x: String) => s
  implicit def string_to_result(s: String) = new CodeFn[B, RFn, String]((x: X) => s, s.toString())

}
trait EngineStringStringTests extends EngineStringTests[String] with Engine1Types[String, String] {
  implicit def string_to_because(s: String) = new Because[B]((x) => x contains s, s.toString())
  implicit def string_to_b(s: String) = (x: String) => x contains s
}
