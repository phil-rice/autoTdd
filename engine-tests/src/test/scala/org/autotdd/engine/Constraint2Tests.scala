package org.autotdd.engine
import org.autotdd.constraints.UseCase
import org.autotdd.engine._
import org.autotdd.constraints._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class Constraint2Tests extends AbstractConstraintTests[(Int, Int) => Boolean, (Int, Int) => Int, Int] {

  def name = "A constraint created by the UseCase 2 method"
  def constraint = Scenario( 1, 2)
  val params: List[Any] = List(1, 2)
  val codeFn = (i: Int, j: Int) => i + j
  val because = (i: Int, j: Int) => true
  val falseBecause = (i: Int, j: Int) => false
  def firstResult = 3

  def checkRfn(c: Constraint[(Int, Int) => Boolean, (Int, Int) => Int, Int]) = {
    assert(2 == c.actualCode.rfn(1, 1))
    assert(-2 == c.actualCode.rfn(-1, -1))
  }

  def checkExpected(c: Constraint[(Int, Int) => Boolean, (Int, Int) => Int, Int]) = {
    assert(firstResult == c.actualCode.rfn(1, 1))
    assert(firstResult == c.actualCode.rfn(-1, 1))
  }

  it should "have a nice to string for the code in byCalling" in {
    val c = constraint.produces(2).byCalling((i, j) => i + j)
    val description = c.code.get.description
    assert(description == "((i: Int, j: Int) => i.+(j))", description)
  }

  it should "have a nice to string for the because" in {
    val c = constraint.produces(2).because((i, j) => true)
    val description = c.because.get.description
    assert(description == "((i: Int, j: Int) => true)", description)
  }
}