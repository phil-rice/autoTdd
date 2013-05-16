package org.autoTdd.engine

import scala.runtime.ZippedTraversable2.zippedTraversable2ToTraversable

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

trait EngineTests extends IfThenParserTestTrait {

  def check(engine: Engine1[String, String], expected: String) {
    val exceptedTree = p(expected)
    val actual = comparator.compare(exceptedTree, engine.root)
    assert(actual == List(), actual + "\nExpected: " + exceptedTree + "\n Actual: " + engine.root + "\nEngine:\n" + engine)
  }
  def checkConstraints(engine: Engine1[String, String], expected: String*) {
    assert(engine.constraints.size == expected.size)
    for ((c, a) <- (engine.constraints, expected).zipped) {
      assert(c.becauseString == a, "Expected: [" + a + "] BecauseString = [" + c.becauseString + "] Actual " + c + "\n   Constraints: " + engine.constraints)
    }
  }
}

class EngineConstructionTest extends FlatSpec with ShouldMatchers with EngineTests {

  it should "add to else path if first constraints doesnt match second" in {
    val engine = Engine1[String, String](default = "Z");
    engine.constraint("A", "X", because = "A");
    engine.constraint("B", "Y", because = "B");
    check(engine, "if a/a then x#a/a else if b/b then y#b/b else z")
    checkConstraints(engine, "A", "B");
  }

  it should "add to then path if second constraint is valid in first" in {
    val engine = Engine1[String, String](default = "Z");
    engine.constraint("A", "X", because = "A");
    engine.constraint("AB", "Y", because = "B");
    check(engine, "if a/a if b/ab then y#b/ab else x#a/a else z")
    checkConstraints(engine, "A", "B");
  }

  it should "keep the order of constraints" in {
    val engine = Engine1[String, String](default = "Z");
    engine.constraint("A", "X", because = "A");
    engine.constraint("C", "X", because = "C");
    engine.constraint("B", "X", because = "B");
    checkConstraints(engine, "A", "C", "B");
  }

  //TODO Consider how to deal with identical result, different because. It's not clear to me what I should do
  it should "throw exception if  cannot differentiate inputs, identical result, different because" in {
    val engine = Engine1[String, String](default = "Z");
    engine.constraint("AB", "X", because = "B");
    evaluating { engine.constraint("AB", "X", because = "A") } should produce[ConstraintConflictException]
  }
  it should "throw exception if  cannot differentiate inputs, different result" in {
    val engine = Engine1[String, String](default = "Z");
    engine.constraint("AB", "Y", because = "B");
    evaluating { engine.constraint("AB", "X", because = "A") } should produce[ConstraintConflictException]
  }

  it should "assertions should add themselves to existing nodes" in {
    val engine = Engine1[String, String](default = "Z");
    engine.constraint("A", "X", because = "A")
    check(engine, "if a/a then x#a/a else z")  
    engine.constraint("AA", "X")
    check(engine, "if a/a then x#/aa,#a/a else z")  
  }

  it should "throw ConstraintConflictException if the added constraint is an assertion and comes to wrong result" in {
    val engine = Engine1[String, String](default = "Z");
    engine.constraint("A", "X", because = "A")
    evaluating { engine.constraint("AA", "Z") } should produce[AssertionException]
  }

}