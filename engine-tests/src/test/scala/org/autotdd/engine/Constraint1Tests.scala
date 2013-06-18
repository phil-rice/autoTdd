package org.autotdd.engine
import org.autotdd.constraints.UseCase
import org.autotdd.engine._
import org.autotdd.constraints._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class Constraint1Tests extends AbstractConstraintTests[(Int) => Boolean, (Int) => Int, Int] with Engine1Types[Int, Int] {

  def name = "A constraint created by the UseCase 1 method"
  def constraint = Scenario(1)
  val params: List[Any] = List(1)
  val codeFn = (i: Int) => i + 1
  val because = (i: Int) => true
  val falseBecause = (i: Int) => false
  def firstResult = 2

  def checkRfn(c: Constraint[(Int) => Boolean, (Int) => Int, Int]) = {
    assert(2 == c.actualCode.rfn(1))
    assert(0 == c.actualCode.rfn(-1))
  }
  def checkExpected(c: Constraint[(Int) => Boolean, (Int) => Int, Int]) = {
    assert(firstResult == c.actualCode.rfn(1))
    assert(firstResult == c.actualCode.rfn(-1))
  }

  it should "have a nice to string for the code in byCalling" in {
    val c = constraint.produces(2).byCalling((i) => i + 1)
    val description = c.code.get.description
    assert(description == "((i: Int) => i.+(1))", description)
  }
  it should "have a nice to string for the because" in {
    val c = constraint.produces(2).because((i) => true)
    val description = c.because.get.description
    assert(description == "((i: Int) => true)", description)
  }

  it should "have an actual code that generates the 'produces' if a byCalling isn't specific" in {
    val c = constraint.produces(2).because((i) => true)
    val cd: Code = c.actualCode
    assert(2 == cd.rfn(1))
    assert(2 == cd.rfn(2))
  }

  it should "have an actual code which is the codeFn is byCalling is specified" in {
    val c = constraint.produces(2).byCalling((i) => 2).because((i) => true)
    val actual = c.actualCode
    val expected = c.code.get
    assert(expected == actual, "WTF")
  }
}