package org.autoTdd.engine

import scala.language.experimental.macros
import scala.reflect.macros.Context
import org.autotdd.constraints.Constraint
import org.autotdd.constraints.CodeFn
import org.autotdd.constraints.Because
import org.autoTdd.engine.tests.EngineTest

class ConstraintBecauseException(msg: String) extends RuntimeException(msg)
class ConstraintResultException(msg: String) extends RuntimeException(msg)
class EngineResultException(msg: String) extends RuntimeException(msg)
class ConstraintConflictException(msg: String) extends RuntimeException(msg)
class AssertionException(msg: String) extends RuntimeException(msg)
//class CannotAccessBrokenEngineException(msg: String) extends RuntimeException(msg)

case class Node[B, RFn, R, C <: Constraint[B, RFn, R, C]](val because: Because[B], val inputs: List[Any], val yes: Either[CodeFn[RFn, C], Node[B, RFn, R, C]], no: Either[CodeFn[RFn, C], Node[B, RFn, R, C]])

trait EngineTypes[R] {
  type B
  type RFn
  type C <: Constraint[B, RFn, R, C]

  type BecauseClosure = (B) => Boolean
  type ResultClosure = (RFn) => R

  type N = Node[B, RFn, R, C]
  type Code = CodeFn[RFn, C]
  type OptN = Option[N]
  type RorN = Either[Code, N]
}

trait EvaluateEngine[R] extends EngineTypes[R] {

  def evaluate(fn: BecauseClosure, n: RorN): RFn = {
    n match {
      case Left(r) => r.rfn
      case Right(n) => evaluate(fn, n)
    }
  }

  private def evaluate(fn: BecauseClosure, n: N): RFn = {
    fn(n.because.because) match {
      case false => evaluate(fn, n.no);
      case true => evaluate(fn, n.yes);
    }
  }
}
trait BuildEngine[R] extends EvaluateEngine[R] {
  def makeClosureForBecause(params: List[Any]): BecauseClosure
  def makeClosureForResult(params: List[Any]): ResultClosure

  def buildFromConstraints(root: RorN, cs: List[C]): RorN = {
    cs.size match {
      case 0 => root;
      case _ => buildFromConstraints(withConstraint(None, root, cs.head, false), cs.tail)
    }
  }

  private def withConstraint(parent: Option[N], n: RorN, c: C, parentWasTrue: Boolean): RorN = {
    val fn = makeClosureForBecause(c.params)
    val fnr = makeClosureForResult(c.params)
    n match {
      case Left(l) =>
        c.because match {
          case Some(b) =>
            parent match {
              case Some(p) =>
                val wouldBreakExisting = parentWasTrue & makeClosureForBecause(p.inputs)(b.because)
                if (wouldBreakExisting)
                  throw new ConstraintConflictException("Cannot differentiate between\nExisting: " + p + "\nConstraint: " + c)
              case _ =>
            }
            Right(Node(b, c.params, Left(c.code.copy(constraints = c :: c.code.constraints)), Left(l)))
          case None =>
            val actualResultIfUseThisNodesCode = fnr(l.rfn);
            if (actualResultIfUseThisNodesCode != c.expected)
              throw new AssertionException("Actual Result: " + actualResultIfUseThisNodesCode + "\nExpected: " + c.expected);
            Left(l.copy(constraints = c :: l.constraints))
        }

      case Right(r) =>
        makeClosureForBecause(c.params)(r.because.because) match {
          case true => Right(r.copy(yes = withConstraint(Some(r), r.yes, c, true)));
          case false => Right(r.copy(no = withConstraint(Some(r), r.no, c, false)));
        }
    }
  }

  private def findLastMatch(fn: BecauseClosure, root: OptN, lastMatch: OptN, params: List[Any]): OptN = {
    root match {
      case None => None
      case Some(r) =>
        fn(r.because.because) match {
          case true => findLastMatch(fn, r.yes, root, params)
          case false => findLastMatch(fn, r.no, lastMatch, params)
        }
    }
  }
  private def findLastMatch(fn: BecauseClosure, root: RorN, lastMatch: OptN, params: List[Any]): OptN = {
    root match {
      case Left(r) => lastMatch
      case Right(n) => findLastMatch(fn, Some(n), lastMatch, params)
    }
  }
}

trait EngineTypesWithRoot[R] extends EngineTypes[R] {
  def root: RorN
}

trait AddConstraints[R] extends EngineTypesWithRoot[R] {
  type CR
  def addConstraint(c: C): CR
}

trait Engine[R] extends BuildEngine[R] with AddConstraints[R] with EvaluateEngine[R] {
  def root: RorN
  def constraints: List[C]
  def realConstraint(c: C): C = c

  private def validateConstraint(c: C) {
    c.because match {
      case Some(b) =>
        if (!makeClosureForBecause(c.params).apply(b.because))
          throw new ConstraintBecauseException(c.becauseString + " is not true for " + c.params);
      case None =>
    }
    val actualFromConstraint = c.actualValueFromParameters
    if (actualFromConstraint != c.expected)
      throw new ConstraintResultException("Wrong result for " + c.code.description + " for " + c.params + "\nActual: " + actualFromConstraint + "\nExpected: " + c.expected);
  }

  private def checkConstraint(c: C) {
    validateConstraint(c);
    val actualFromEngine = applyParam(c.params);
    if (actualFromEngine != c.expected)
      throw new EngineResultException("Wrong result for " + c.code.description + " for " + c.params + "\nActual: " + actualFromEngine + "\nExpected: " + c.expected);
  }

  def addConstraintWithChecking(c: C, addingClosure: (C) => CR, default: CR): CR = {
    try {
      val result = addingClosure(c)
      validateConstraint(c)
      if (!EngineTest.testing)
        checkConstraint(c) //check later
      result
    } catch {
      case e: Throwable if EngineTest.testing =>
        EngineTest.exceptions = EngineTest.exceptions + (c -> e); default
      case e: Throwable => throw e
    }
  }

  def applyParam(params: List[Any]): R = {
    val rfn = evaluate(makeClosureForBecause(params), root)
    makeClosureForResult(params)(rfn)
  }

}

trait EngineToString[R] extends EngineTypesWithRoot[R] {

  def toString(indent: String, root: RorN): String = {
    root match {
      case Left(result) => indent + result.description + "\n"
      case Right(node) =>
        indent + "if(" + node.because.becauseString + ")\n" +
          toString(indent + " ", node.yes) +
          indent + "else\n" +
          toString(indent + " ", node.no)
    }
  }
  override def toString: String = toString("", root)
}

trait ImmutableEngine[R] extends Engine[R] {
  type E
  type CR = E
  def defaultResult: R

  def newEngine(defaultResult: R, constraints: List[C]): E
  def oldEngine: E
  def addConstraint(c: C): CR = {
    addConstraintWithChecking(c, (c) => {
      val newConstraints = (c :: constraints).reverse; //could do better..
      val result = newEngine(defaultResult, newConstraints);
      result
    }, oldEngine)
  }
}

abstract class MutableEngine[R]() extends Engine[R] {
  type CR = R
  var constraints = List[C]()
  val defaultRoot: Code
  var root: RorN
  
  def addConstraint(c: C): CR = {
    addConstraintWithChecking(c, (c) => {
      val l = c :: constraints.reverse
      val newConstraints = l.reverse
      constraints = newConstraints
      root = buildFromConstraints(Left(defaultRoot), constraints);
      c.expected
    }, c.expected);
  }
}

