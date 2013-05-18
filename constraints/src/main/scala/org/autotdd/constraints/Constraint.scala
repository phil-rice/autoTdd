package org.autotdd.constraints

import scala.reflect.macros.Context
import scala.language.experimental.macros

abstract class Constraint[B, RFn, R, C](val expected: R, val code: CodeFn[RFn, C], val because: Option[Because[B]]) {
  def params: List[Any]
  def actualValueFromParameters: R
  def hasDefaultBecause = because.isEmpty
  def becauseString = because match { case Some(b) => b.description; case _ => "" }
}

//TODO Need better extraction of parameters as the parameters could be functions
abstract class CodeHolder(val description: String) {
  private val index = description.indexOf("=>");
  val pretty = index match {
    case -1 => description
    case i => description.substring(index + 3, description.length - 1)
  }
  val parameters = index match {
    case -1 => description
    case i => description.substring(0, index);
  }

}

case class CodeFn[RFn, C](val rfn: RFn, override val description: String, val constraints: List[C] = List[C]()) extends CodeHolder(description)

object CodeFn {
  implicit def r_to_result[RFn, C](r: RFn): CodeFn[RFn, C] = macro c_to_code_impll[RFn, C]

  def c_to_code_impll[RFn: c.WeakTypeTag, C: c.WeakTypeTag](c: Context)(r: c.Expr[RFn]): c.Expr[CodeFn[RFn, C]] = {
    import c.universe._
    reify { CodeFn[RFn, C](r.splice, c.literal(show(r.tree)).splice) }
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

