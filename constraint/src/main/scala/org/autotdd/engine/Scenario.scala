package org.autotdd.engine

import scala.Option.option2Iterable
import scala.reflect.macros.Context
import scala.language.implicitConversions
import scala.language.experimental.macros

class EngineException(msg: String, cause: Throwable) extends Exception(msg, cause) {
  def this(msg: String) = this(msg, null)
}
class InvalidBecauseException(msg: String) extends EngineException(msg, null)
class NoExpectedException(msg: String) extends EngineException(msg, null)

case class Configurator[K](item: K, fn: (K) => Unit);

object RfnMaker {
  def rfn1ConstantMaker[P, R] = (e: Either[Exception, R]) => e match { case Left(e) => (p: P) => throw e; case Right(r) => (p: P) => r }
  def rfn2ConstantMaker[P1, P2, R] = (e: Either[Exception, R]) => e match { case Left(e) => (p1: P1, p2: P2) => throw e; case Right(r) => (p1: P1, p2: P2) => r }
  def rfn3ConstantMaker[P1, P2, P3, R] = (e: Either[Exception, R]) => e match { case Left(e) => (p1: P1, p2: P2, p3: P3) => throw e; case Right(r) => (p1: P1, p2: P2, p3: P3) => r }

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

  override def toString = getClass.getSimpleName() + "(" + description + ")"

}

case class CodeFn[B, RFn, R](val rfn: RFn, override val description: String) extends CodeHolder(description) {
  override def toString = getClass.getSimpleName() + "(" + description + ")"
}

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

