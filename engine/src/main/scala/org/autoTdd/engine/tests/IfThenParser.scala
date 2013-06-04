package org.autotdd.engine.tests

import scala.util.parsing.combinator._
import scala.util.parsing.combinator.JavaTokenParsers

import org.autotdd.constraints._
import org.autotdd.constraints.UseCase
import org.autotdd.engine._

object IfThenParser {

  def parser1[P, R](
    becauses: Map[String, Because[(P) => Boolean]],
    inputs: Map[String, List[Any]],
    constraints: Map[String, Constraint[(P) => Boolean, (P) => R, R]],
    thens: Map[String, CodeFn[(P) => Boolean, (P) => R, R]]) = {
    new IfThenParser[R]() with Engine1Types[P, R].actualParser(becauses, inputs, constraints, thens, UseCase.rfn1ConstantMaker);
  }
  def parser2[P1, P2, R](becauses: Map[String, Because[(P1, P2) => Boolean]],
    inputs: Map[String, List[Any]],
    constraints: Map[String, Constraint[(P1, P2) => Boolean, (P1, P2) => R, R]],
    thens: Map[String, CodeFn[(P1, P2) => Boolean, (P1, P2) => R, R]]) =
    new IfThenParser[R]() with Engine2Types[P1, P2, R].actualParser(becauses, inputs, constraints, thens, UseCase.rfn2ConstantMaker)
}

trait IfThenParser[R] extends EngineTypes[R] {

  def actualParser(becauses: Map[String, Because[B]], inputs: Map[String, List[Any]], constraints: Map[String, C], thens: Map[String, Code], rfnMaker: RFnMaker) = new ActualParser(becauses, inputs, constraints, thens, rfnMaker);

  case class ActualParser(val becauses: Map[String, Because[B]], val inputs: Map[String, List[Any]], val constraints: Map[String, C], val thens: Map[String, Code], rfnMaker: RFnMaker) extends JavaTokenParsers with Function[String, RorN] {

    case class B_Inputs(b: Option[Because[B]], l: List[Any])

    def resultOrifThenElse: Parser[RorN] = ifThenElse | result
    def ifThenElse: Parser[RorN] = "if" ~> because ~ inputList ~ yes ~ "else" ~ no ^^ { case b ~ i ~ y ~ e ~ n => Right(Node(b, i, y, n)) }

    def because: Parser[Because[B]] = ident ^^ (becauses(_))

    def inputList: Parser[List[Any]] = opt("/" ~ input) ^^ {
      case Some(l ~ i) => i;
      case None => List()
    }
    def input: Parser[List[Any]] = ident ^^ (inputs(_))

    def yes: Parser[RorN] = ("then" ~> result | ifThenElse)
    def no: Parser[RorN] = resultOrifThenElse

    def result: Parser[RorN] = ident ~ constraintList ^^ {
      case r ~ cl =>
        val then: Code = thens(r);
        Left(then.copy(constraints = then.constraints ++
          cl.collect({
            case B_Inputs(b, l) =>
              val expected: R = makeClosureForResult(l)(then.rfn)
              val because: Option[Because[B]] = b
              new ConstraintBuilder[B, RFn, R]("", l, None, None, b, List(),rfnMaker).byCallingCode(then)
          })))
    }
    def constraintList: Parser[List[B_Inputs]] = repsep(constraint, ",")
    def constraint: Parser[B_Inputs] = "#" ~> opt(because) ~ inputList ^^ { case b ~ i => B_Inputs(b, i) }

    override def apply(raw: String): RorN =
      parseAll(resultOrifThenElse, raw).get;
  }

}