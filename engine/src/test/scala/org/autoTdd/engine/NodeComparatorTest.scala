package org.autoTdd.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class NodeComparatorTest extends FlatSpec with ShouldMatchers with IfThenParserTestTrait {

  "A node comparator" should "return empty list if the node/results are both identical results" in {
    val x1 = p("x");
    val x2 = p("x");
    val actual = comparator.compare(x1, x2)
    assert(actual == List())
  }
  it should "return 'result X Y' if the node/results are different results" in {
    val x1 = p("x");
    val x2 = p("y");
    val actual = comparator.compare(x1, x2)
    assert(actual == List("result X Y"), actual)
  }

  it should "return empty list if the trees are both identical" in {
    val x1 = p("if a then x else y");
    val x2 = p("if a then x else y");
    val actual = comparator.compare(x1, x2)
    assert(actual == List())
  }

  it should "return 'because A B' if the trees have different becauses" in {
    val x1 = p("if a then x else y");
    val x2 = p("if b then x else y");
    check(x1, x2, List("because A B"))
  }

  it should "return description if left is result and right is node " in {
    val x1 = p("x");
    val x2 = p("if a then x else y");
    check(x1, x2, List("left is result X Right is tree " + x2.right.get))
  }

  it should "return description if left is a node and right is a result " in {
    val x1 = p("if a then x else y");
    val x2 = p("x");
    check(x1, x2, List("left is tree " + x1.right.get + " Right is result X"))
  }

  it should "report errors in yes branch" in {
    val x1 = p("if a if b then x else y else z");
    val x2 = p("if a if c then x else y else z");
    check(x1, x2, List("yes/because B C"))
  }

  it should "report errors in no branch" in {
    val x1 = p("if a then x else if b then y else z");
    val x2 = p("if a then x else if c then y else z");
    check(x1, x2, List("no/because B C"))
  }

  it should "report errors if constraint size mismatch " in {
    val x1 = p("if a/a if b then w#a/ab else x else y")
    val x2 = p("if a/a if b then w else x else y")
    check(x1, x2, List("yes/yes/constraints/ sizes 1,0"))
  }
  it should "report errors if constraint inputs mismatch " in {
    val x1 = p("if a/a if b then w#b/aa else x else y")
    val x2 = p("if a/a if b then w#b/b else x else y")
    check(x1, x2, List("yes/yes/constraints/[B]params List(AA), List(B)"))
  }
  it should "report errors if constraint because  mismatch " in {
    val x1 = p("if a/a if b then w#aa/b else x else y")
    val x2 = p("if a/a if b then w#b/b else x else y")
    check(x1, x2, List("yes/yes/constraints/[AA]because AA, B"))
  }

  def check(x1: RorN, x2: RorN, expected: List[String]) {
    val actual = comparator.compare(x1, x2)
    assert(actual == expected, actual)

  }
}