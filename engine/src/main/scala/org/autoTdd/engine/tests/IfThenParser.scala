package org.autoTdd.engine.tests

import scala.util.parsing.combinator.JavaTokenParsers
import scala.util.parsing.combinator._
import org.autotdd.constraints.CodeFn
import org.autotdd.constraints.Because
import org.autoTdd.engine.Constraint2
import org.autoTdd.engine.Engine1Types
import org.autoTdd.engine.EngineTypes
import org.autoTdd.engine.Engine2Types
import org.autoTdd.engine.Constraint1
import org.autoTdd.engine.Node

object IfThenParser {

  def parser1[P, R](becauses: Map[String, Because[(P) => Boolean]], inputs: Map[String, Any], constraints: Map[String, Constraint1[P, R]], thens: Map[String, CodeFn[(P) => R, Constraint1[P, R]]]) = {
    new IfThenParser[R]() with Engine1Types[P, R].actualParser(becauses, inputs, constraints, thens,
      (b, i, r) => { val p = i(0).asInstanceOf[P]; new Constraint1[P, R](p, r.rfn(p), r, b) })
  }
  def parser2[P1, P2, R](becauses: Map[String, Because[(P1, P2) => Boolean]], inputs: Map[String, Any], constraints: Map[String, Constraint2[P1, P2, R]], thens: Map[String, CodeFn[(P1, P2) => R, Constraint2[P1, P2, R]]]) =
    new IfThenParser[R]() with Engine2Types[P1, P2, R].actualParser(becauses, inputs, constraints, thens,
      (b, i, r) => { val p1 = i(0).asInstanceOf[P1]; val p2 = i(1).asInstanceOf[P2]; new Constraint2[P1, P2, R](p1, p2, r.rfn(p1, p2), r, b) })
}

trait IfThenParser[R] extends EngineTypes[R] {

  def actualParser(becauses: Map[String, Because[B]], inputs: Map[String, Any], constraints: Map[String, C], thens: Map[String, Code], constraintMaker: (Option[Because[B]], List[Any], Code) => C) = new ActualParser[R](becauses, inputs, constraints, thens, constraintMaker);

  case class ActualParser[R](val becauses: Map[String, Because[B]], val inputs: Map[String, Any], val constraints: Map[String, C], val thens: Map[String, Code], constraintMaker: (Option[Because[B]], List[Any], Code) => C) extends JavaTokenParsers with Function[String, RorN] {

    case class B_Inputs(var b: Option[Because[B]], l: List[Any])

    def resultOrifThenElse: Parser[RorN] = ifThenElse | result
    def ifThenElse: Parser[RorN] = "if" ~> because ~ inputList ~ yes ~ "else" ~ no ^^ { case b ~ i ~ y ~ e ~ n => Right(Node(b, i, y, n)) }

    def because: Parser[Because[B]] = ident ^^ (becauses(_))

    def inputList: Parser[List[Any]] = opt("/" ~ repsep(input, ",")) ^^ { case Some(l ~ i) => i; case None => List() }
    def input: Parser[Any] = ident ^^ (inputs(_))

    def yes: Parser[RorN] = ("then" ~> result | ifThenElse)
    def no: Parser[RorN] = resultOrifThenElse

    def result: Parser[RorN] = ident ~ constraintList ^^ { case r ~ cl => 
      val then: Code = thens(r); 
      Left(then.copy(constraints = then.constraints ++ 
          cl.map({ 
            case B_Inputs(b, l) => 
              constraintMaker(b, l, then) }))) }
    def constraintList: Parser[List[B_Inputs]] = repsep(constraint, ",")
    def constraint: Parser[B_Inputs] = "#" ~> opt(because) ~ inputList ^^ { case b ~ i => B_Inputs(b, i) }

    override def apply(raw: String): RorN =
      parseAll(resultOrifThenElse, raw).get;
  }

}