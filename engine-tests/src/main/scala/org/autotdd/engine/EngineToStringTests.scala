package org.autotdd.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.autotdd.engine._

class EngineToStringTests extends FlatSpec with ShouldMatchers with PosNegTestTrait with EngineTests {

  "An engine with no constraints " should "show the default value " in {
    assertToStringMatches(Engine1[Int, String]("Zero"), "Zero\n");
    assertToStringMatches(Engine1[Int, String]((i: Int) => "Zero"), "((i: Int) => \"Zero\")\n");
  }

  "An engine with one constraint" should "have a decent to string" in {
    val engine = org.autotdd.engine.Engine1[Int, String]((i: Int) => "Zero")
    engine.constraint(1, "Pos", (x) => "Pos", (x) => x > 0)
    assertToStringMatches(engine, "if(((x: Int) => x.>(0)))\n" +
      " ((x: Int) => \"Pos\")\n" +
      "else\n" +
      " ((i: Int) => \"Zero\")\n");
  }

  "An engine with the pos negs " should "have a decent to string " in {
    makeAndCheckToString(
      "if(((x: Int) => x.>(0)))\n" +
        " if(((x: Int) => x.>(50)))\n" +
        "  ((x: Int) => \"VBigPos\")\n" +
        " else\n" +
        "  if(((x: Int) => x.>(5)))\n" +
        "   ((x: Int) => \"BigPos\")\n" +
        "  else\n" +
        "   ((x: Int) => \"Pos\")\n" +
        "else\n" +
        " if(((x: Int) => x.<(-50)))\n" +
        "  ((x: Int) => \"VBigNeg\")\n" +
        " else\n" +
        "  if(((x: Int) => x.<(-5)))\n" +
        "   ((x: Int) => \"BigNeg\")\n" +
        "  else\n" +
        "   if(((x: Int) => x.<(0)))\n" +
        "    ((x: Int) => \"Neg\")\n" +
        "   else\n" +
        "    Zero\n", pos, vBigPos, vBigNeg, bigNeg, neg, bigPos);
  }
}