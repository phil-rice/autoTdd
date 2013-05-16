package org.autoTdd.engine

import scala.util.Either
import scala.util.Left
import org.autotdd.constraints.CodeFn
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.autotdd.constraints.Because
import org.autoTdd.engine.tests.NodeComparator
import org.autoTdd.engine.tests.IfThenParser

trait IfThenParserTestTrait extends Engine1Types[String, String] with ShouldMatchers {

  implicit def string_to_because(s: String) = new Because[B]((x) => x contains s, s.toString())
  implicit def string_to_result(s: String) = new CodeFn[RFn, C]((x) => s, s.toString())
  implicit def string_to_constraint(s: String) = new Constraint1[String, String](s, s, s, Some(s))

  def node(b: B, inputs: List[Any], yes: RorN, no: RorN) = new Node(b, inputs, yes, no);
  def rightNode(b: Because[B], inputs: List[Any], yes: RorN, no: RorN) = Right(new Node(b, inputs, yes, no));

  val p = IfThenParser.parser1[String, String](
    becauses = Map("a" -> "A", "aa" -> "AA", "b" -> "B", "c" -> "C"),
    inputs = Map("a" -> "A", "b" -> "B", "c" -> "C", "aa" -> "AA", "ab" -> "AB"),
    constraints = Map("c1" -> "1", "c2" -> "2"),
    thens = Map("w" -> "W", "x" -> "X", "y" -> "Y", "z" -> "Z"))
  def comparator = NodeComparator.comparator1[String, String]

  def assertMatches(n1: RorN, n2: RorN) {
    val actual = comparator.compare(n1, n2)
    assert(actual == List(), actual)
  }
}

class IfThenBuilderTest extends FlatSpec with ShouldMatchers with IfThenParserTestTrait {

  "An If Then Builder " should " return a result if just result specified" in {
    assertMatches(p("x"), Left("X"))
  }

  it should "parse a simple If then else statement, substituting Becauses and Thens" in {
    assertMatches(p("if a then x else y"), rightNode("A", List(), Left("X"), Left("Y")))
  }

  it should "parse an If then else statement with inputs" in {
    assertMatches(p("if a/a,b then x else y"), rightNode("A", List("A", "B"), Left("X"), Left("Y")))
  }

  it should "parse nested if then else statements " in {
    assertMatches(p("if a/a,b if b then w else x else y"), rightNode("A", List("A", "B"), rightNode("B", List(), Left("W"), Left("X")), Left("Y")))
  }

  it should "produce extra constraints" in {
    val a = Constraint1[String, String]("A", "X", "X", Some("A"))
    val aBlank = Constraint1[String, String]("A", "X", "X", None)
    val b = Constraint1[String, String]("C", "X", "X", Some("B"))

    val x: Code = "X"

    assertMatches(p("if a then x#b/c else y"), rightNode("A", List(), Left(x.copy(constraints = List(b))), Left("Y")))
    assertMatches(p("if a/a then x#a/a else z"), rightNode("A", List("A"), Left(x.copy(constraints = List(a))), Left("Z")))
    assertMatches(p("if a/a then x#/a else z"), rightNode("A", List("A"), Left(x.copy(constraints = List(aBlank))), Left("Z")))
    assertMatches(p("if a then x#b/c,#a/a else y"), rightNode("A", List(), Left(x.copy(constraints = List(b, a))), Left("Y")))
  }

}