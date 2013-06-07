package org.autotdd.engine

import org.autotdd.constraints._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

case class Holder(var value: String);
@RunWith(classOf[JUnitRunner])
class EngineMutabilityTests extends EngineStringTests[Holder] {

  val mutable = Holder("A");

  "An engine passed a mutable parameter" should "reset the paramaters prior to checking the because " in {
    val becauseA = new Because[B]((h => h.value == "A"), "hA");
    val becauseB = new Because[B]((h => h.value == "B"), "hB");
    val a = Scenario( mutable).produces("XA").becauseBecause(becauseA).whenConfigured(mutable, (a: Holder) => a.value = "A")
    val b = Scenario( mutable).produces("XB").becauseBecause(becauseB).whenConfigured(mutable, (a: Holder) => a.value = "B")
    val engine = Engine1[Holder, String]("Z", UseCase("", a, b))
    engine.validateConstraints

  }

}