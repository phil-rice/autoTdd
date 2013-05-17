package org.autotdd.engine

import scala.language.experimental.macros
import scala.reflect.macros.Context
import org.autotdd.constraints.Constraint
import org.autotdd.constraints.CodeFn
import org.autotdd.constraints.Because

case class Constraint2[P1, P2, R](val p1: P1, val p2: P2, override val expected: R, override val code: CodeFn[(P1, P2) => R, Constraint2[P1, P2, R]], override val because: Option[Because[(P1, P2) => Boolean]])
  extends Constraint[(P1, P2) => Boolean, (P1, P2) => R, R, Constraint2[P1, P2, R]](expected, code, because) {
  override def params = List(p1, p2)
  def actualValueFromParameters = code.rfn(p1, p2)
}

object Engine2 {

  def apply[P1, P2, R](default: R) = new MutableEngine2[P1, P2, R](CodeFn((p1: P1, p2: P2) => default, default.toString))
  def apply[P1, P2, R](default: (P1, P2) => R) = new MutableEngine2[P1, P2, R](default)
  def mutable[P1, P2, R](default: R) = new MutableEngine2[P1, P2, R]((p1: P1, p2: P2) => default)
  def immutable[P1, P2, R](default: (P1, P2) => R) = new ImmutableEngine2[P1, P2, R](default, List())

  def constraint_impl_with_code[P1: c.WeakTypeTag, P2: c.WeakTypeTag, R: c.WeakTypeTag, CR: c.WeakTypeTag](c: Context)(p1: c.Expr[P1], p2: c.Expr[P2], expected: c.Expr[R], code: c.Expr[(P1, P2) => R], because: c.Expr[(P1, P2) => Boolean]): c.Expr[CR] = {
    import c.universe._
    val expr = reify {
      val be: Because[(P1, P2) => Boolean] = Because[(P1, P2) => Boolean](because.splice, c.literal(show(because.tree)).splice);
      val cd = CodeFn[(P1, P2) => R, Constraint2[P1, P2, R]](code.splice, c.literal(show(code.tree)).splice, List());
      (c.Expr[Engine2[P1, P2, R]](c.prefix.tree)).splice.raw_constraint(p1.splice, p2.splice, expected.splice, cd, be);
    }
    c.Expr[CR](expr.tree)
  }

  def constraint_impl_with_just_expected[P1: c.WeakTypeTag, P2: c.WeakTypeTag, R: c.WeakTypeTag, CR: c.WeakTypeTag](c: Context)(p1: c.Expr[P1], p2: c.Expr[P2], expected: c.Expr[R], because: c.Expr[(P1, P2) => Boolean]): c.Expr[CR] = {
    import c.universe._
    val expr = reify {
      val be: Because[(P1, P2) => Boolean] = Because[(P1, P2) => Boolean](because.splice, c.literal(show(because.tree)).splice);
      val cd = CodeFn[(P1, P2) => R, Constraint2[P1, P2, R]]((p1, p2) => expected.splice, c.literal(show(expected.tree)).splice, List());
      (c.Expr[Engine2[P1, P2, R]](c.prefix.tree)).splice.raw_constraint(p1.splice, p2.splice, expected.splice, cd, be);
    }
    c.Expr[CR](expr.tree)
  }

}

trait Engine2Types[P1, P2, R] extends EngineTypes[R] {
  type B = (P1, P2) => Boolean
  type RFn = (P1, P2) => R
  type C = Constraint2[P1, P2, R]

}

trait Engine2[P1, P2, R] extends Engine[R] with Function2[P1, P2, R] with EngineToString[R] with Engine2Types[P1, P2, R] {
  def apply(p1: P1, p2: P2): R = {
    val rFn = evaluate(b => b(p1, p2), root)
    rFn(p1, p2)
  }

  def makeClosureForBecause(params: List[Any]) = (b) => b(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2])
  def makeClosureForResult(params: List[Any]) = (r) => r(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2])
  def justBecause = (p1: P1, p2: P2) => true

  def assertion(p1: P1, p2: P2, expected: R): CR = raw_constraint(p1, p2, expected, null, null)
  def constraint(p1: P1, p2: P2, expected: R): CR = raw_constraint(p1, p2, expected, null, null)

  def constraint(p1: P1, p2: P2, expected: R, because: (P1, P2) => Boolean) = macro Engine2.constraint_impl_with_just_expected[P1, P2, R, CR]
  def constraint(p1: P1, p2: P2, expected: R, code: (P1, P2) => R, because: (P1, P2) => Boolean) = macro Engine2.constraint_impl_with_code[P1, P2, R, CR]

  def raw_constraint(p1: P1, p2: P2, expected: R, code: Code, because: Because[B]): CR = {
    val b = because match { case null => None; case x => Some(x) }
    if (code == null)
      addConstraint(realConstraint(Constraint2(p1, p2, expected, CodeFn[RFn, C]((p1: P1, p2: P2) => expected, expected.toString), b)))
    else
      addConstraint(realConstraint(Constraint2(p1, p2, expected, code, b)))
  }

  def makeDefaultRoot(defaultRoot: R): RorN =
    Left(CodeFn((p1, p2) => defaultRoot, defaultRoot match {
      case null => "null"
      case _ => defaultRoot.toString
    }))
}

class ImmutableEngine2[P1, P2, R](val defaultResult: CodeFn[(P1, P2) => R, Constraint2[P1, P2, R]], val constraints: List[Constraint2[P1, P2, R]]) extends Engine2[P1, P2, R] {
  type CR = ImmutableEngine2[P1, P2, R]
  val root: RorN = buildFromConstraints(Left(defaultResult), constraints)
  def addConstraint(c: C): CR = this
}

class MutableEngine2[P1, P2, R](val defaultRoot: CodeFn[(P1, P2) => R, Constraint2[P1, P2, R]]) extends MutableEngine[R] with Engine2[P1, P2, R] {
  var root: RorN = Left(defaultRoot);
} 