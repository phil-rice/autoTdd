package org.autotdd.engine

import org.autotdd.constraints._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Shell

case class Holder(var value: String);
@RunWith(classOf[JUnitRunner])
class EngineMutabilityTests extends EngineStringTests[Holder] {

  val mutable = Holder("A");
  val becauseA = new Because[B]((h => h.value contains "A"), "hA");
  val becauseB = new Because[B]((h => h.value contains "B"), "hB");
  val becauseAB = new Because[B]((h => (h.value contains "A") & (h.value contains "B")), "hAB");

  "An engine passed a mutable parameter" should "reset the paramaters prior to checking the because 1" in {
    val a = Scenario(mutable).produces("XA").becauseBecause(becauseA).whenConfigured(mutable, (a: Holder) => a.value = "A")
    val b = Scenario(mutable).produces("XB").becauseBecause(becauseB).whenConfigured(mutable, (a: Holder) => a.value = "B")
    val ab = Scenario(mutable).produces("XB").becauseBecause(becauseAB).whenConfigured(mutable, (a: Holder) => a.value = "AB")
    Engine1[Holder, String]("Z", UseCase("", a, ab)).validateConstraints
    Engine1[Holder, String]("Z", UseCase("", ab, a)).validateConstraints
    Engine1[Holder, String]("Z", UseCase("", a, ab, b)).validateConstraints
    Engine1[Holder, String]("Z", UseCase("", a, b, ab)).validateConstraints
    Engine1[Holder, String]("Z", UseCase("", ab, a, b)).validateConstraints
    Engine1[Holder, String]("Z", UseCase("", ab, b, a)).validateConstraints
    Engine1[Holder, String]("Z", UseCase("", b, a, ab)).validateConstraints
    Engine1[Holder, String]("Z", UseCase("", b, ab, a)).validateConstraints
    println("Engine: "+ Engine1[Holder, String]("Z", UseCase("", a, ab)))
    fail()
  }

  
}