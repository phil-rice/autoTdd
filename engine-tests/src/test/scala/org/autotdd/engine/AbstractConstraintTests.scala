
package org.autotdd.engine

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.autotdd.engine._
import org.autotdd.constraints._

abstract class AbstractConstraintTests[B, RFn, R] extends FlatSpec with ShouldMatchers {

  def name: String
  def constraint: ConstraintBuilder[B, RFn, R]
  def description: String = "Some description"
  def params: List[Any]
  def codeFn: RFn
  def because: B
  def falseBecause: B
  def firstResult: R
  def checkRfn(c: Constraint[B, RFn, R]): Unit
  def checkExpected(c: Constraint[B, RFn, R]): Unit

  name should " remember parameters and have None for the other parameters" in {
    val c = constraint
    assert(c.params == params)
    assert(c.code == None)
    assert(c.because == None)
    assert(c.expected == None)
    assert(c.configuration == List())
  }

  it should "start with description empty, then get description from scenarioHolder if it has one" in {
    val c = constraint
    val sh = new ScenarioHolder {
      def description = "root";
      def indexOf[B, RFn, R](c: Constraint[B, RFn, R]) = 2
    }
    val c2 = constraint.withScenarioHolder(sh)
    assert(c.description == "")
    assert(c.scenarioHolder == null)
    val x = c2.description;
    assert(c2.description == "root[2]")
    assert(c2.scenarioHolder == sh)
  }

  it should "add expected when produces is called" in {
    val c = constraint.produces(firstResult)
    assert(c.params == params)
    assert(c.code == None)
    assert(c.because == None)
    assert(c.expected == Some(firstResult))
    assert(c.configuration == List())
  }

  it should "add code when byCalling is called" in {
    val c = constraint.produces(firstResult).byCalling(codeFn)
    assert(c.params == params)
    val expectedCode = Some(new CodeFn(codeFn, "AbstractConstraintTests.this.codeFn"))
    assert(c.code == expectedCode, "Constraint: " + c + "\nCode: " + c.code + "\nExpected: " + expectedCode)
    assert(c.because == None)
    assert(c.expected == Some(firstResult))
    assert(c.configuration == List())
  }

  it should "add because when because is called" in {
    val c = constraint.produces(firstResult).because(because)
    assert(c.params == params)
    assert(c.code == None)
    val expected = Some(new Because(because, "AbstractConstraintTests.this.because"))
    assert(c.because == expected, c.because)
    assert(c.expected == Some(firstResult))
    assert(c.configuration == List())
  }

  it should "use expected as actualCode if code isn't specified" in {
    val c = constraint.produces(firstResult)
    checkExpected(c);
  }

  it should "use code as actualCode if specified" in {
    val c = constraint.produces(firstResult).byCalling(codeFn)
    checkRfn(c)
  }

}