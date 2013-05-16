package org.autoTdd.engine

import org.scalatest.FlatSpec

import org.scalatest.matchers.ShouldMatchers

class EngineFirstConstraintTests extends FlatSpec with ShouldMatchers with EngineTests {
  "An empty engine" should "change from root to if then with one constraint" in {
    val engine = Engine1[String, String](default = "Z");
    engine.constraint("A", "X", because = "A");
    check(engine, "if a/a then x#a/a else z")
    checkConstraints(engine, "A");
  }

  it should "Add constraint to root if adding assertion" in {
    val engine = Engine1[String, String](default = "X");
    val initialRoot = engine.root.left.get
    engine.constraint("A", "X")
    check(engine, "x#/a")
    assertMatches(engine.root, Left(initialRoot.copy(constraints = engine.constraints)))
  }

  it should "Throw AssertionException if first constraint is assertion and comes to wrong result" in {
    val engine = Engine1[String, String](default = "Z");
    evaluating { engine.constraint("A", "X") } should produce[AssertionException]
  }
}