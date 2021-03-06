package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.autotdd.engine._

@RunWith(classOf[JUnitRunner])
class EngineAssertionTests extends EngineStringStringTests {

  "Assertions" should "be executed one when testing" in {
    EngineTest.test { () => checkAssertionExecuted(1) }
  }

  "Assertions" should "be executed twice when not testing - one once building scenario, and once at end " in {
    checkAssertionExecuted(2)
  }

  def checkAssertionExecuted(times: Int) {
    var count = 0
    val a: A = (p: String, r:  ROrException[String]) => {
      count += 1;
      true
    }
    val assertion = new Assertion[A](assertion = a, description = "descr")

    val bldr = builderWithDefault.scenario("AB").because(becauseA).expected("X").assertion(assertion);

    assertEquals(0, count)
    val e = bldr.build
    assertEquals(times, count)
  }

  "Assertions" should "cause the build to fail with an AssertionException if they return false " in {
    val assertion = new Assertion[A](assertion = (p, r) => false, description = "descr")
    val bldr = builderWithDefault.scenario("AB").because(becauseA).expected("X").assertion(assertion);
    val e = evaluating { val e = bldr.build } should produce[AssertionException]

    assertEquals("\nAssertion descr failed.\nParams are List(AB)\nResult was X", e.getMessage())

  }

}