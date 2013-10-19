package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ReuseExpectTests extends EngineStringStringTests {

  "A Builder" should "store the last expected" in {
    val b1 = builderWithUseCase.
      scenario("A").expected("X")
    assertEquals(ROrException[String]("X"), b1.lastExpect)
  }

  it should "forget the last expected when a new use case starts" in {
    val b1 = builderWithUseCase.
      scenario("A").expected("X").useCase("new")
    assertEquals(ROrException[String](), b1.lastExpect)

  }

  "An engine" should "Use last expected if no expected is given" in {
    val e = builderWithUseCase.
      scenario("A").expected("X").because("A").
      scenario("B").because("B").
      scenario("C").expected("Y").because("C").build
    assertEquals("X", e("A"))
    assertEquals("X", e("B"))
    assertEquals("Y", e("C"))
  }

  it should "use expected even if it occurs before the scenario" in {
    val e = builderWithUseCase.
      expected("X").
      scenario("A").because("A").
      scenario("B").because("B").
      build
    assertEquals("X", e("A"))
    assertEquals("X", e("B"))
  }

  "An engine" should "Use last expected if no expected is given, even when it's the last scenario" in {
    val e = builderWithUseCase.
      scenario("A").expected("X").because("A").
      scenario("B").because("B").build
    assertEquals("X", e("A"))
    assertEquals("X", e("B"))
  }
  "An engine" should "Use exected if expected is given" in {

  }
  "An engine" should "throw NoExpectedException if no expected given" in {

  }

  "An engine" should "throw NoExpectedException if no expected given in this use case, even if expected given in previous use cases" in {

  }
}