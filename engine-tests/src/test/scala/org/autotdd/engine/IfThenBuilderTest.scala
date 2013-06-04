package org.autotdd.engine
import scala.util.Left
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.autotdd.engine.tests._
import org.autotdd.constraints.CodeFn
import org.autotdd.constraints._
import org.autotdd.engine._

trait IfThenParserTestTrait extends Engine1Types[String, String] with ShouldMatchers {

  //  implicit def string_to_becauseFn(s: String) = (x: String) => x contains s
  implicit def string_to_because(s: String) = new Because[B]((x) => x contains s, s.toString())
  implicit def string_to_b(s: String) = (x: String) => x contains s
  implicit def string_to_rfn(s: String) = (x: String) => s
  implicit def string_to_result(s: String) = new CodeFn[B, RFn, String]((x: String) => s, s.toString())
  implicit def string_to_constraint(s: String) = UseCase(s, s).produces(s).because((x) => x contains s)

  def node(b: B, inputs: List[Any], yes: RorN, no: RorN) = new Node(b, inputs, yes, no);
  def rightNode(b: Because[B], inputs: List[Any], yes: RorN, no: RorN) = Right(new Node(b, inputs, yes, no));

  val p = IfThenParser.parser1[String, String](
    becauses = Map("a" -> "A", "aa" -> "AA", "b" -> "B", "c" -> "C"),
    inputs = Map("a" -> List("A"), "b" -> List("B"), "c" -> List("C"), "aa" -> List("A", "A"), "ab" -> List("A", "B"), "ab" -> List("A", "B")),
    constraints = Map("c1" -> "1", "c2" -> "2"),
    thens = Map("w" -> "W", "x" -> "X", "y" -> "Y", "z" -> "Z"))
  def comparator = NodeComparator.comparator1[String, String]

  def assertMatches(n1: RorN, n2: RorN) {
    val actual = comparator.compare(n1, n2)
    assert(actual == List(), actual)
  }
  def assertEngineMatches(e: Engine[String], n2: RorN) {
    val actual = comparator.compare(e.root.asInstanceOf[RorN], n2)
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
    assertMatches(p("if a/ab then x else y"), rightNode("A", List("A", "B"), Left("X"), Left("Y")))
  }

  it should "parse nested if then else statements " in {
    assertMatches(p("if a/ab if b then w else x else y"), rightNode("A", List("A", "B"), rightNode("B", List(), Left("W"), Left("X")), Left("Y")))
  }

  it should " produce extra constraints of the form 'x#b/c' meaning 'inputs were c and the because is b put they will produce X" in {
    val a = UseCase[String, String]("Irrelevant", "A").becauseBecause("A").byCallingCode("X")
    val b = UseCase[String, String]("Irrelevant", "C").becauseBecause("B").byCallingCode("X")
    assertMatches(p("if a then x#b/c else y"), rightNode("A", List(), Left(("X": Code).copy(constraints = List(b))), Left("Y")))
    assertMatches(p("if a/a then x#a/a else z"), rightNode("A", List("A"), Left(("X": Code).copy(constraints = List(a))), Left("Z")))
    assertMatches(p("if a then x#b/c else y"), rightNode("A", List(), Left(("X": Code).copy(constraints = List(b))), Left("Y")))

  }
  it should "produce multiple extra constraints" in {
    val a = UseCase[String, String]("", "A").becauseBecause("A").byCallingCode("X");
    val b = UseCase[String, String]("", "C").becauseBecause("B").byCallingCode("X")
    val x: Code = "X"
    assertMatches(p("if a then x#b/c,#a/a else y"), rightNode("A", List(), Left(x.copy(constraints = List(b, a))), Left("Y")))
  }

}