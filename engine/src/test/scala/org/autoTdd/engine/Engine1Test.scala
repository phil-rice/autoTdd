package org.autoTdd.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class Engine1Test extends FlatSpec with ShouldMatchers with PosNegTestTrait {

  "An empty Engine" should "return the default value" in {
    val engine_1 = Engine1[Int, Int](default = 1);
    assert(1 == engine_1(234))

    val engine_2 = Engine1[Int, Int](default = 2);
    assert(2 == engine_2(123))
  }

  "An engine with one constraint" should "apply that constraint or return the default" in {
    val engine = Engine1[Int, String](default = "Negative");
    val result = (p) => "P" + p
    val because = (x: Int) => x >= 0
    val expected = "P1"
    val actual: Any = engine.constraint(1, expected, result, because);
    assert(engine(-1) == "Negative")
    assert(actual == "P1")
    assert(engine(0) == "P0")
    assert(engine(1) == "P1")
    assert(engine(100) == "P100")
    assert(engine(-1) == "Negative")
    assert(engine(-100) == "Negative")
  }

  "A constraint without a result" should "return expected value when it applies" in {
    val engine = Engine1[Int, String](default = "Negative");
    val actual: String = engine.constraint(1, "Positive", because = (x: Int) => x >= 0);
    assert(actual == "Positive")
    assert(engine(0) == "Positive")
    assert(engine(1) == "Positive")
    assert(engine(100) == "Positive")
    assert(engine(-1) == "Negative")
    assert(engine(-100) == "Negative")
  }

  "An engine" should "throw ConstraintResultException if the expected value is not returned from the result function" in {
    val engine = Engine1[Int, String](default = "Negative");
    evaluating { val x: String = engine.constraint(1, "PX", (p: Int) => "P" + p, (x: Int) => x >= 0) } should produce[ConstraintResultException]
  }

  it should "throw ConstraintBecauseException if the because function is not true" in {
    val engine = Engine1[Int, String](default = "Negative");
    evaluating { val x: String = engine.constraint(1, "P1", (p: Int) => "P" + p, (x: Int) => x < 0) } should produce[ConstraintBecauseException]
  }

  "A constraint " should "have a becauseString that is the AST of the because parameter serialized" in {
    val engine: MutableEngine1[Int, String] = Engine1[Int, String](default = "Negative").asInstanceOf[MutableEngine1[Int, String]];
    engine.constraint(1, "P1", (p: Int) => "P" + p, because = (x: Int) => x >= 0)
    assert(engine.constraints.size == 1)
    val c = engine.constraints.head
    assert(c.becauseString == "((x: Int) => x.>=(0))", c.becauseString) //Note I don't know why I have an extra () and a '.' but I'm not complaining 
  }

  it should "ignore constraints if the result is already derived " in {
    val engine = Engine1[Int, String](default = "Negative");
    engine.constraint(1, "Positive", because = (x: Int) => x >= 0);
    engine.constraint(2, "Positive");
    assert(engine(1) == "Positive")
    assert(engine(2) == "Positive")
    assert(engine(-1) == "Negative")
  }
  "An engine" should "allow constraints to be added one at a time " in {
    //       makeAndCheck(bigPos, pos, bigNeg, neg);
    val engine = Engine1[Int, String](default = "Zero");
    engine.addConstraint(bigPos);
    engine.addConstraint(pos);
    engine.addConstraint(bigNeg);
    engine.addConstraint(neg);
    engine.addConstraint(vBigPos);
    engine.addConstraint(vBigNeg);
  }

  "An engine " should "apply four constraints, whatever the order, in this smoke test" in {
    makeAndCheck(pos, bigPos, neg, bigNeg);
    makeAndCheck(neg, bigNeg, pos, bigPos);
    makeAndCheck(bigPos, pos, bigNeg, neg);
    makeAndCheck(bigNeg, neg, bigPos, pos);
  }

  it should "apply constraints, whatever the order, in this smoke test" in {
    makeAndCheck(pos, bigPos, neg, bigNeg, vBigPos, vBigNeg);
    makeAndCheck(neg, bigNeg, pos, bigPos, vBigPos, vBigNeg);
    makeAndCheck(bigPos, pos, bigNeg, neg, vBigPos, vBigNeg);
    makeAndCheck(bigNeg, neg, bigPos, pos, vBigPos, vBigNeg);
    makeAndCheck(vBigPos, vBigNeg, bigNeg, neg, bigPos, pos);
  }

  it should "have a decent to string " in {
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

  def makeAndCheckToString(expected: String, constraints: Constraint1[Int, String]*) = {
    val engine = Engine1[Int, String](default = "Zero");
    for (c <- constraints)
      engine.addConstraint(c);
    val actual = engine.toString
    assert(expected == actual, "Expected\n[" + expected + "]\nActual:\n[" + actual + "]")
  }

  def makeAndCheck(constraints: Constraint1[Int, String]*) = {
    val engine = Engine1[Int, String](default = "Zero");
    for (c <- constraints)
      engine.addConstraint(c);
    for (c <- constraints) {
      val p = c.param
      assert(c.expected == engine(p), "\nEngine:\n" + engine + "\nConstraint: " + c);
    }
  }
}