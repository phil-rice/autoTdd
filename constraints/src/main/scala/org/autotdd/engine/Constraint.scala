package org.autotdd.constraints

import scala.reflect.macros.Context

import scala.language.experimental.macros

class ConstraintException (msg: String) extends Exception(msg)
class InvalidBecauseException (msg: String) extends ConstraintException(msg)
class NoExpectedException(msg: String) extends ConstraintException(msg)

case class Configurator[K](item: K, fn: (K) => Unit);

object UseCase {
  def rfn1ConstantMaker[P, R] = (e: Either[Exception, R]) => e match { case Left(e) => (p: P) => throw e; case Right(r) => (p: P) => r }
  def rfn2ConstantMaker[P1, P2, R] = (e: Either[Exception, R]) => e match { case Left(e) => (p1: P1, p2: P2) => throw e; case Right(r) => (p1: P1, p2: P2) => r }
  
  def apply[P, R](description: String, p: P) = new ConstraintBuilder[(P) => Boolean, (P) => R, R](description, List(p), None, None, None, List(), rfn1ConstantMaker);
  def apply[P1, P2, R](description: String, p1: P1, p2: P2) = new ConstraintBuilder[(P1, P2) => Boolean, (P1, P2) => R, R](description, List(p1, p2), None, None, None, List(), rfn2ConstantMaker);
}
class UseCase


trait Constraint[B, RFn, R] {
  def description: String
  def expected: Option[R]
  def code: Option[CodeFn[B, RFn, R]]
  def because: Option[Because[B]]
  def configuration: List[Configurator[Any]]
  def params: List[Any]
  def hasDefaultBecause = because.isEmpty
  def becauseString = because match { case Some(b) => b.description; case _ => "" }
  def rfnMaker: (Either[Exception, R]) => RFn
  def configure =
    for (
      param <- params;
      value <- configuration.find((c) => c.item == param)
    ) yield value.fn(param)
  val actualCode: CodeFn[B, RFn, R] = code.getOrElse({
    //    println("Expected: " + expected)
    //    println("Constraint: " + this)
    expected match {
      case Some(e) => new CodeFn(rfnMaker(Right(e)), e.toString, List());
      case _ => new CodeFn(rfnMaker(Left(new IllegalStateException("Do not have code or expected  for this constraint"))), "No expected or Code", List())
    }
  })
}

object ConstraintBuilder {

  def byCallingMacroImpl[B: c.WeakTypeTag, RFn: c.WeakTypeTag, R: c.WeakTypeTag](c: Context)(code: c.Expr[RFn]) = {
    import c.universe._
    reify {
      val codeFn = new CodeFn[B, RFn, R](code.splice, c.literal(show(code.tree)).splice, List());
      (c.Expr[ConstraintBuilder[B, RFn, R]](c.prefix.tree)).splice.byCallingCode(codeFn);
    }
  }
  def becauseMacroImpl[B: c.WeakTypeTag, RFn: c.WeakTypeTag, R: c.WeakTypeTag](c: Context)(b: c.Expr[B]) = {
    import c.universe._
    reify {
      val because = new Because[B](b.splice, c.literal(show(b.tree)).splice);
      (c.Expr[ConstraintBuilder[B, RFn, R]](c.prefix.tree)).splice.becauseBecause(because);
    }
  }

}

case class ConstraintBuilder[B, RFn, R](val description: String, val params: List[Any], val expected: Option[R], val code: Option[CodeFn[B, RFn, R]], val because: Option[Because[B]], val configuration: List[Configurator[Any]] , rfnMaker: (Either[Exception, R]) => RFn) extends Constraint[B, RFn, R] {
  def whenConfigured[K](item: K, fn: (K) => Unit) = copy(configuration = Configurator[K](item, fn).asInstanceOf[Configurator[Any]] :: configuration)
  def produces(expected: R) = copy(expected = Some(expected))

  def byCalling[K](code: RFn) = macro ConstraintBuilder.byCallingMacroImpl[B, RFn, R]
  def byCallingCode[K](code: CodeFn[B, RFn, R]) = copy(code = Some(code))

  def because(b: B): ConstraintBuilder[B, RFn, R] = macro ConstraintBuilder.becauseMacroImpl[B, RFn, R];
  def becauseBecause(b: Because[B]): ConstraintBuilder[B, RFn, R] =
    copy(because = Some(b))
}

abstract class CodeHolder(val description: String) {
  private val index = description.indexOf("=>");
  val pretty = index match {
    case -1 => description
    case i => description.substring(index + 3, description.length - 1)
  }
  //TODO Need better extraction of parameters as the parameters could be functions
  val parameters = index match {
    case -1 => description
    case i => description.substring(0, index);
  }

}

case class CodeFn[B, RFn, R](val rfn: RFn, override val description: String, val constraints: List[Constraint[B, RFn, R]] = List[Constraint[B, RFn, R]]()) extends CodeHolder(description)

object CodeFn {
  implicit def r_to_result[B, RFn, R](r: RFn): CodeFn[B, RFn, R] = macro c_to_code_impll[B, RFn, R]

  def c_to_code_impll[B: c.WeakTypeTag, RFn: c.WeakTypeTag, R: c.WeakTypeTag](c: Context)(r: c.Expr[RFn]): c.Expr[CodeFn[B, RFn, R]] = {
    import c.universe._
    reify { CodeFn[B, RFn, R](r.splice, c.literal(show(r.tree)).splice) }
  }

}

case class Because[B](val because: B, override val description: String) extends CodeHolder(description)

object Because {
  implicit def b_to_because[B](b: B): Because[B] = macro b_to_because_imp[B]

  def b_to_because_imp[B: c.WeakTypeTag](c: Context)(b: c.Expr[B]): c.Expr[Because[B]] = {
    import c.universe._
    val becauseString = show(b.tree)
    reify { Because[B](b.splice, c.literal(becauseString).splice) }
  }

}


