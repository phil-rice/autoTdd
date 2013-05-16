package org.autoTdd.engine

import scala.language.experimental.macros
import scala.reflect.macros.Context
import org.autotdd.constraints.Constraint
import org.autotdd.constraints.CodeFn
import org.autotdd.constraints.Because

case class Constraint1[P, R](val param: P, override val expected: R, override val code: CodeFn[(P) => R, Constraint1[P, R]], override val because: Option[Because[(P) => Boolean]])
  extends Constraint[(P) => Boolean, (P) => R, R, Constraint1[P, R]](expected, code, because) {
  override def params = List(param)

  def actualValueFromParameters = code.rfn(param)
}

object Engine1 {
  def apply[P, R](default: R) = new MutableEngine1[P, R](CodeFn((p: P) => default, default.toString))
  def apply[P, R](default: CodeFn[(P) => R, Constraint1[P,R]]) = new MutableEngine1[P, R](default)
  def mutable[P, R](default: (P) => R) = new MutableEngine1[P, R](default)
  def immutable[P, R](default: R = null) = new ImmutableEngine1[P, R](List())

  def blankTrue[P, R](r: R): (P) => Boolean = (p) => true;
  def blankR[P, R](r: R): (P) => R = (p) => r;

}

trait Engine1Types[P, R] extends EngineTypes[R] {
  type B = (P) => Boolean
  type RFn = (P) => R
  type C = Constraint1[P, R]

}

trait Engine1[P, R] extends Engine[R] with Function1[P, R] with EngineToString[R] with Engine1Types[P, R] {

  def apply(p: P): R = evaluate(b => b(p), root)(p)

  def assertion(p: P, expected: R): CR = constraint(p, expected, null, null)

  def constraint(p: P, expected: R, code: Code = null, because: Because[B] = null): CR = {
    val b = because match { case null => None; case x => Some(x) }
    if (code == null)
      addConstraint(realConstraint(Constraint1(p, expected, CodeFn[RFn, C]((p: P) => expected, expected.toString), b)))
    else
      addConstraint(realConstraint(Constraint1(p, expected, code, b)))
  }

  def justBecause = (p: P) => true
  def makeClosureForBecause(params: List[Any]) = (b) => b(params(0).asInstanceOf[P])
  def makeClosureForResult(params: List[Any]) = (r) => r(params(0).asInstanceOf[P])

  def makeDefaultRoot(defaultRoot: R): RorN = Left(CodeFn[RFn, C]((p: P) => defaultRoot, defaultRoot.toString))
}

class ImmutableEngine1[P, R](val constraints: List[Constraint1[P, R]]) extends Engine1[P, R] {
  type CR = ImmutableEngine1[P, R]
  def root = null
  def addConstraint(c: Constraint1[P, R]) = this
}

class MutableEngine1[P, R](val defaultRoot: CodeFn[(P) => R, Constraint1[P, R]]) extends MutableEngine[R] with Engine1[P, R] {
  var root: RorN = Left(defaultRoot)
} 