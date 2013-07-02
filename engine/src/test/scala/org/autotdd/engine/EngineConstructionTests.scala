package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EngineConstructionStringTest extends EngineStringStringTests {

  val wCString = "Z:UseCase1[0]\n"

  val w_aCString = "if(hA)\n" +
    " XA:UseCase1[1]\n" +
    "else\n" +
    " Z:UseCase1[0]\n"

  val w_b_aCString = "if(hA)\n" +
    " XA:UseCase1[1]\n" +
    "else\n" +
    " if(hB)\n" +
    "  XB:UseCase1[2]\n" +
    " else\n" +
    "  Z:UseCase1[0]\n"

  val w_ab_b_aCString = "if(hA)\n" +
    " if(hAB)\n" +
    "  XAB:UseCase1[3]\n" +
    " else\n" +
    "  XA:UseCase1[1]\n" +
    "else\n" +
    " if(hB)\n" +
    "  XB:UseCase1[2]\n" +
    " else\n" +
    "  Z:UseCase1[0]\n"

  "An engine's construction string" should "be included in scenario conflict exceptions to help explain how the exception happened" in {
    val bldrA = builderWithDefault.
      scenario("AB").because(becauseA).expected("X")
    val bldr = bldrA.
      scenario("AB").because(becauseA).expected("Y")
    val e = evaluating { bldr.build } should produce[ScenarioConflictException]
    assert(e.getMessage().contains(bldrA.build.constructionString), "Message: " + e.getMessage())
  }

  it should "return the aggregate of the toString of an engine created from the scenarios, one after another" in {
    val bldr = builderWithDefault.
      scenario("A").because(becauseA).expected("XA").
      scenario("B").because(becauseB).expected("XB").
      scenario("AB").because(becauseAB).expected("XAB")
    val e = bldr.build

    assertEquals(wCString + "\n" + w_aCString + "\n" + w_b_aCString + "\n" + w_ab_b_aCString, e.constructionString)
  }

  "An engine's increasingScenariosList method" should "return a list of increasing numbers of scenarios" in {
    val bldr = builderWithDefault.
      scenario("A").because(becauseA).expected("XA").
      scenario("B").because(becauseB).expected("XB").
      scenario("AB").because(becauseAB).expected("XAB")
    val e = bldr.build
    val w = e.scenarios(0); assertEquals(List("W"), w.params)
    val a = e.scenarios(1); assertEquals(List("A"), a.params)
    val b = e.scenarios(2); assertEquals(List("B"), b.params)
    val ab = e.scenarios(3); assertEquals(List("AB"), ab.params)

    val actual = e.increasingScenariosList(e.scenarios)
    val expected = List(List(ab, b, a, w), List(b, a, w), List(a, w), List(w))
    assertEquals(expected, actual)
  }

  "An engine's buildRoot method" should "return a node that represents the scenarios" in {
    val bldr = builderWithDefault.
      scenario("A").because(becauseA).expected("XA").
      scenario("B").because(becauseB).expected("XB").
      scenario("AB").because(becauseAB).expected("XAB")
    val e = bldr.build
    val w = e.scenarios(0); assertEquals(List("W"), w.params)
    val a = e.scenarios(1); assertEquals(List("A"), a.params)
    val b = e.scenarios(2); assertEquals(List("B"), b.params)
    val ab = e.scenarios(3); assertEquals(List("AB"), ab.params)

    val actual = e.buildRoot(e.defaultRoot, List(w, a, b, ab))
    assertEngineMatches(e, actual)
  }

}