package org.autotdd.engine

import scala.language.experimental.macros
import scala.reflect.macros.Context
import org.autotdd.constraints.Constraint
import org.autotdd.constraints.CodeFn
import org.autotdd.constraints.Because

trait Engine2WithStateTypes[P1, P2, S, O] extends EngineTypes[(S, O)] {
  type B = (P1, P2, S) => Boolean
  type RFn = (P1, P2, S) => (S, O)
  type C = Constraint2WithState[P1, P2, S, O]

}
case class Constraint2WithState[P1, P2, S, O](val p1: P1, val p2: P2, val sIn: S,
  override val expected: (S, O),
  override val code: CodeFn[(P1, P2, S) => (S, O), Constraint2WithState[P1, P2, S, O]],
  override val because: Option[Because[(P1, P2, S) => Boolean]])
  extends Constraint[(P1, P2, S) => Boolean, (P1, P2, S) => (S, O), (S, O), Constraint2WithState[P1, P2, S, O]](expected, code, because) {
  override def params = List(p1, p2, sIn)
  def actualValueFromParameters = code.rfn(p1, p2, sIn)
}

object Engine2WithState {

  //  def apply[P1, P2, R](default: R) = new MutableEngine2[P1, P2, R](CodeFn((p1: P1, p2: P2) => default, default.toString))
  def apply[P1, P2, S, O](default: (P1, P2, S) => (S, O)) = macro create_from_default_function[P1, P2, S, O]

  def create_from_default_function[P1: c.WeakTypeTag, P2: c.WeakTypeTag, S: c.WeakTypeTag, O: c.WeakTypeTag](c: Context)(default: c.Expr[(P1, P2, S) => (S, O)]): c.Expr[MutableEngine2WithState[P1, P2, S, O]] = {
    import c.universe._
    val expr = reify { new MutableEngine2WithState[P1, P2, S, O](new CodeFn[(P1, P2, S) => (S, O), Constraint2WithState[P1, P2, S, O]](default.splice, c.literal(show(default.tree)).splice)) }
    c.Expr[MutableEngine2WithState[P1, P2, S, O]](expr.tree)
  }

  def constraint_impl_with_code[P1: c.WeakTypeTag, P2: c.WeakTypeTag, S: c.WeakTypeTag, O: c.WeakTypeTag, CR: c.WeakTypeTag](c: Context)(p1: c.Expr[P1], p2: c.Expr[P2], sIn: c.Expr[S], expected: c.Expr[(S, O)], code: c.Expr[(P1, P2, S) => (S, O)], because: c.Expr[(P1, P2, S) => Boolean]): c.Expr[CR] = {
    import c.universe._
    val expr = reify {
      val be: Because[(P1, P2, S) => Boolean] = Because[(P1, P2, S) => Boolean](because.splice, c.literal(show(because.tree)).splice);
      val cd = CodeFn[(P1, P2, S) => (S, O), Constraint2WithState[P1, P2, S, O]](code.splice, c.literal(show(code.tree)).splice, List());
      (c.Expr[Engine2WithState[P1, P2, S, O]](c.prefix.tree)).splice.raw_constraint(p1.splice, p2.splice, sIn.splice, expected.splice, cd, be);
    }
    c.Expr[CR](expr.tree)
  }

  def constraint_impl_with_just_expected[P1: c.WeakTypeTag, P2: c.WeakTypeTag, S: c.WeakTypeTag, O: c.WeakTypeTag, CR: c.WeakTypeTag](c: Context)(p1: c.Expr[P1], p2: c.Expr[P2], sIn: c.Expr[S], expected: c.Expr[(S, O)], because: c.Expr[(P1, P2, S) => Boolean]): c.Expr[CR] = {
    import c.universe._
    val expr = reify {
      val be: Because[(P1, P2, S) => Boolean] = Because[(P1, P2, S) => Boolean](because.splice, c.literal(show(because.tree)).splice);
      val cd = CodeFn[(P1, P2, S) => (S, O), Constraint2WithState[P1, P2, S, O]]((p1, p2, sIn) => expected.splice, c.literal(show(expected.tree)).splice, List());
      (c.Expr[Engine2WithState[P1, P2, S, O]](c.prefix.tree)).splice.raw_constraint(p1.splice, p2.splice, sIn.splice, expected.splice, cd, be);
    }
    c.Expr[CR](expr.tree)
  }

}

trait Engine2WithState[P1, P2, S, O] extends Engine[(S, O)] with Function3[P1, P2, S, (S, O)] with EngineToString[(S, O)] with Engine2WithStateTypes[P1, P2, S, O] {
  def apply(p1: P1, p2: P2, sIn: S): (S, O) = {
    val rFn = evaluate(b => b(p1, p2, sIn), root)
    rFn(p1, p2, sIn)
  }

  def makeClosureForBecause(params: List[Any]) = (b) => b(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[S])
  def makeClosureForResult(params: List[Any]) = (r) => r(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[S])
  def justBecause = (p1: P1, p2: P2) => true

  def assertion(p1: P1, p2: P2, sIn: S, expected: (S, O)): CR = raw_constraint(p1, p2, sIn, expected, null, null)
  def constraint(p1: P1, p2: P2, sIn: S, expected: (S, O)): CR = raw_constraint(p1, p2, sIn, expected, null, null)

  def constraint(p1: P1, p2: P2, sIn: S, expected: (S, O), because: (P1, P2, S) => Boolean) = macro Engine2WithState.constraint_impl_with_just_expected[P1, P2, S, O, CR]
  def constraint(p1: P1, p2: P2, sIn: S, expected: (S, O), code: (P1, P2, S) => (S, O), because: (P1, P2, S) => Boolean) = macro Engine2WithState.constraint_impl_with_code[P1, P2, S, O, CR]

  def raw_constraint(p1: P1, p2: P2, sIn: S, expected: (S, O), code: Code, because: Because[B]): CR = {
    val b = because match { case null => None; case x => Some(x) }
    if (code == null)
      addConstraint(realConstraint(Constraint2WithState(p1, p2, sIn, expected, CodeFn[RFn, C]((p1: P1, p2: P2, sIn: S) => expected, expected.toString), b)))
    else
      addConstraint(realConstraint(Constraint2WithState(p1, p2, sIn, expected, code, b)))
  }

  def makeDefaultRoot(defaultRoot: (S,O)): RorN =
    Left(CodeFn((p1, p2, sIn) => defaultRoot, defaultRoot match {
      case null => "null"
      case _ => defaultRoot.toString
    }))
}

class ImmutableEngine2WithState[P1, P2, S, O](val defaultResult: CodeFn[(P1, P2, S) => (S, O), Constraint2WithState[P1, P2, S, O]], val constraints: List[Constraint2WithState[P1, P2, S, O]]) extends Engine2WithState[P1, P2, S, O] {
  type CR = ImmutableEngine2WithState[P1, P2, S, O]
  val root: RorN = buildFromConstraints(Left(defaultResult), constraints)
  def addConstraint(c: C): CR = this
}

class MutableEngine2WithState[P1, P2, S, O](val defaultRoot: CodeFn[(P1, P2, S) => (S,O), Constraint2WithState[P1, P2, S, O]]) extends MutableEngine[(S, O)] with Engine2WithState[P1, P2, S, O] {
  var root: RorN = Left(defaultRoot);
} 
