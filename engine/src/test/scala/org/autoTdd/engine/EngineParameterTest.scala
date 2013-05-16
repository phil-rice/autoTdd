package org.autoTdd.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class EngineParameterTest extends FlatSpec with ShouldMatchers with EngineTests {

  "A constraint" should "record the parameters used" in {
    val c = Constraint1[String, String]("a", "expected", (x: String) => "expected", Some((x: String) => true))
    assert("((x: String) " == c.because.get.parameters, c.because.get.parameters)
  }

  "A constraint created by an engine" should "record the parameters used" in {
    val engine = Engine1[String, String](default = "Z");
    engine.constraint("A", "X", because = (x: String) => true);
    val c = engine.constraints.head
    assert("((x: String) " == c.because.get.parameters, c.because.get.parameters)
  }

}