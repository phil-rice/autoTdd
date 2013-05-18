package org.autotdd.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.autotdd.engine._

class EngineToStringTests extends FlatSpec with ShouldMatchers with PosNegTestTrait with EngineTests {

  "An engine with no constraints " should "show the default value " in {
    assertToStringMatches(Engine1[Int, String]("Zero"), "Zero\n");
    assertToStringMatches(Engine1[Int, String]((i: Int) => "Zero"), "\"Zero\"\n");
  }

  "An engine with one constraint" should "have a decent to string" in {
    val engine = org.autotdd.engine.Engine1[Int, String]((i: Int) => "Zero")
    engine.constraint(1, "Pos", (x) => "Pos", (x) => x > 0)
    assertToStringMatches(engine, "if(x.>(0))\n" +
      " \"Pos\"\n" +
      "else\n" +
      " \"Zero\"\n");
  }

  "An engine with the pos negs " should "have a decent to string " in {
    makeAndCheckToString(
      "if(x.>(0))\n" +
        " if(x.>(50))\n" +
        "  \"VBigPos\"\n" +
        " else\n" +
        "  if(x.>(5))\n" +
        "   \"BigPos\"\n" +
        "  else\n" +
        "   \"Pos\"\n" +
        "else\n" +
        " if(x.<(-50))\n" +
        "  \"VBigNeg\"\n" +
        " else\n" +
        "  if(x.<(-5))\n" +
        "   \"BigNeg\"\n" +
        "  else\n" +
        "   if(x.<(0))\n" +
        "    \"Neg\"\n" +
        "   else\n" +
        "    Zero\n", pos, vBigPos, vBigNeg, bigNeg, neg, bigPos);
  }
}