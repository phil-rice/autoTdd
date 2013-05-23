package org.autotdd.engine

import scala.language.experimental.macros
import scala.reflect.macros.Context
import org.autotdd.constraints.Constraint
import org.autotdd.constraints.CodeFn
import org.autotdd.constraints.Because

trait Engine3Types[P1, P2, P3, R] extends EngineTypes[R] {
  type B = (P1, P2, P3) => Boolean
  type RFn = (P1, P2, P3) => R
  type C = Constraint3[P1, P2, P3, R]

}
case class Constraint3[P1, P2, P3, R](val p1: P1, val p2: P2, val p3: P3, override val expected: R, override val code: CodeFn[(P1, P2, P3) => R, Constraint3[P1, P2, P3, R]], override val because: Option[Because[(P1, P2, P3) => Boolean]])
  extends Constraint[(P1, P2, P3) => Boolean, (P1, P2, P3) => R, R, Constraint3[P1, P2, P3, R]](expected, code, because) {
  override def params = List(p1, p2, p3)
  def actualValueFromParameters = code.rfn(p1, p2, p3)
}

object Engine3 {

  //  def apply[P1, P2, R](default: R) = new MutableEngine3[P1, P2, R](CodeFn((p1: P1, p2: P2) => default, default.toString))
  def apply[P1, P2, P3, R](default: (P1, P2, P3) => R) = macro create_from_default_function[P1, P2, P3, R]

  def create_from_default_function[P1: c.WeakTypeTag, P2: c.WeakTypeTag, P3: c.WeakTypeTag, R: c.WeakTypeTag](c: Context)(default: c.Expr[(P1, P2, P3) => R]): c.Expr[MutableEngine3[P1, P2, P3, R]] = {
    import c.universe._
    val expr = reify { new MutableEngine3[P1, P2, P3, R](new CodeFn[(P1, P2, P3) => R, Constraint3[P1, P2, P3, R]](default.splice, c.literal(show(default.tree)).splice)) }
    c.Expr[MutableEngine3[P1, P2, P3, R]](expr.tree)
  }

  def constraint_impl_with_code[P1: c.WeakTypeTag, P2: c.WeakTypeTag, P3: c.WeakTypeTag, R: c.WeakTypeTag, CR: c.WeakTypeTag](c: Context)(p1: c.Expr[P1], p2: c.Expr[P2], p3: c.Expr[P3], expected: c.Expr[R], code: c.Expr[(P1, P2, P3) => R], because: c.Expr[(P1, P2, P3) => Boolean]): c.Expr[CR] = {
    import c.universe._
    val expr = reify {
      val be: Because[(P1, P2, P3) => Boolean] = Because[(P1, P2, P3) => Boolean](because.splice, c.literal(show(because.tree)).splice);
      val cd = CodeFn[(P1, P2, P3) => R, Constraint3[P1, P2, P3, R]](code.splice, c.literal(show(code.tree)).splice, List());
      (c.Expr[Engine3[P1, P2, P3, R]](c.prefix.tree)).splice.raw_constraint(p1.splice, p2.splice, p3.splice, expected.splice, cd, be);
    }
    c.Expr[CR](expr.tree)
  }

  def constraint_impl_with_just_expected[P1: c.WeakTypeTag, P2: c.WeakTypeTag, P3: c.WeakTypeTag, R: c.WeakTypeTag, CR: c.WeakTypeTag](c: Context)(p1: c.Expr[P1], p2: c.Expr[P2], p3: c.Expr[P3], expected: c.Expr[R], because: c.Expr[(P1, P2, P3) => Boolean]): c.Expr[CR] = {
    import c.universe._
    val expr = reify {
      val be: Because[(P1, P2, P3) => Boolean] = Because[(P1, P2, P3) => Boolean](because.splice, c.literal(show(because.tree)).splice);
      val cd = CodeFn[(P1, P2, P3) => R, Constraint3[P1, P2, P3, R]]((p1, p2, p3) => expected.splice, c.literal(show(expected.tree)).splice, List());
      (c.Expr[Engine3[P1, P2, P3, R]](c.prefix.tree)).splice.raw_constraint(p1.splice, p2.splice, p3.splice, expected.splice, cd, be);
    }
    c.Expr[CR](expr.tree)
  }

}

trait Engine3[P1, P2, P3, R] extends Engine[R] with Function3[P1, P2, P3, R] with EngineToString[R] with Engine3Types[P1, P2, P3, R] {
  def apply(p1: P1, p2: P2, p3: P3): R = {
    val rFn = evaluate(b => b(p1, p2, p3), root)
    rFn(p1, p2, p3)
  }

  def makeClosureForBecause(params: List[Any]) = (b) => b(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[P3])
  def makeClosureForResult(params: List[Any]) = (r) => r(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[P3])
  def justBecause = (p1: P1, p2: P2) => true

  def assertion(p1: P1, p2: P2, p3: P3, expected: R): CR = raw_constraint(p1, p2, p3, expected, null, null)
  def constraint(p1: P1, p2: P2, p3: P3, expected: R): CR = raw_constraint(p1, p2, p3, expected, null, null)

  def constraint(p1: P1, p2: P2, p3: P3, expected: R, because: (P1, P2, P3) => Boolean) = macro Engine3.constraint_impl_with_just_expected[P1, P2, P3, R, CR]
  def constraint(p1: P1, p2: P2, p3: P3, expected: R, code: (P1, P2, P3) => R, because: (P1, P2, P3) => Boolean) = macro Engine3.constraint_impl_with_code[P1, P2, P3, R, CR]

  def raw_constraint(p1: P1, p2: P2, p3: P3, expected: R, code: Code, because: Because[B]): CR = {
    val b = because match { case null => None; case x => Some(x) }
    if (code == null)
      addConstraint(realConstraint(Constraint3(p1, p2, p3, expected, CodeFn[RFn, C]((p1: P1, p2: P2, p3: P3) => expected, expected.toString), b)))
    else
      addConstraint(realConstraint(Constraint3(p1, p2, p3, expected, code, b)))
  }

  def makeDefaultRoot(defaultRoot: R): RorN =
    Left(CodeFn((p1, p2, p3) => defaultRoot, defaultRoot match {
      case null => "null"
      case _ => defaultRoot.toString
    }))
}

class ImmutableEngine3[P1, P2, P3, R](val defaultResult: CodeFn[(P1, P2, P3) => R, Constraint3[P1, P2, P3, R]], val constraints: List[Constraint3[P1, P2, P3, R]]) extends Engine3[P1, P2, P3, R] {
  type CR = ImmutableEngine3[P1, P2, P3, R]
  val root: RorN = buildFromConstraints(Left(defaultResult), constraints)
  def addConstraint(c: C): CR = this
}

class MutableEngine3[P1, P2, P3, R](val defaultRoot: CodeFn[(P1, P2, P3) => R, Constraint3[P1, P2, P3, R]]) extends MutableEngine[R] with Engine3[P1, P2, P3, R] {
  var root: RorN = Left(defaultRoot);
} 
