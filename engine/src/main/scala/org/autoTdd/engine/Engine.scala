package org.autotdd.engine

import scala.language.experimental.macros

import scala.reflect.macros.Context

import org.autotdd.constraints._
class ConstraintBecauseException(msg: String) extends EngineException(msg)
class ConstraintResultException(msg: String) extends EngineException(msg)
class EngineResultException(msg: String) extends EngineException(msg)
class ConstraintConflictException(msg: String) extends EngineException(msg)
class AssertionException(msg: String) extends EngineException(msg)
class ExceptionAddingConstraint(msg: String, t: Throwable) extends EngineException(msg, t)
//class CannotAccessBrokenEngineException(msg: String) extends RuntimeException(msg)

/** There are a load of generics / types flying around. This class defines classes, and is the mechanism for avoiding code duplication with different arities / shapes of implementations */
trait EngineTypes[R] {
  /** B is a function from the parameters of the engine to a boolean. It is effectively in calculating which constraint is to be used */
  type B
  /** RFn is a function from the parameters of the engine to a result R. It is used to calculate the result of the engine */
  type RFn
  /**  */
  type C = Constraint[B, RFn, R]

  type RFnMaker = (Either[Exception, R]) => RFn

  /** In order to call a B in the building code, when we don't know anything about the arity, we create a closure holding the parameters and pass the B function to it */
  type BecauseClosure = (B) => Boolean
  /** In order to call a RFN in the building code, when we don't know anything about the arity, we create a closure holding the parameters and pass the B function to it */
  type ResultClosure = (RFn) => R

  /** This is just a type synonym to save messy code */
  type N = Node[B, RFn, R]
  /** This is just a type synonym to save messy code */
  type Code = CodeFn[B, RFn, R]
  /** This is just a type synonym to save messy code */
  type OptN = Option[N]
  /** This is just a type synonym to save messy code.  This represents the decision tree: either a result or another node which has a condition, and a true/false RorN  */
  type RorN = Either[Code, N]
  def makeClosureForBecause(params: List[Any]): BecauseClosure
  def makeClosureForResult(params: List[Any]): ResultClosure
}

case class Node[B, RFn, R](val because: Because[B], inputs: List[Any], yes: Either[CodeFn[B, RFn, R], Node[B, RFn, R]], no: Either[CodeFn[B, RFn, R], Node[B, RFn, R]], constraintThatCausedNode: Constraint[B, RFn, R])

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
trait BuildEngine[R] extends EvaluateEngine[R] with EngineToString[R] {

  def buildFromConstraints(root: RorN, cs: List[C]): RorN = {
    cs.size match {
      case 0 => root;
      case _ => buildFromConstraints(withConstraint(None, root, cs.head, false), cs.tail)
    }
  }

  def evaluateBecauseForConstraint(c: C, params: List[Any]) = {
    val fn = makeClosureForBecause(params)
    c.configure
    c.because match { case Some(b) => fn(b.because); case _ => throw new IllegalStateException("No because in " + c) }
  }
  def evaluateResultForConstraint(c: C, params: List[Any]) = {
    val fnr = makeClosureForResult(params)
    c.configure
    try {
      Left(fnr(c.actualCode.rfn));
    } catch {
      case e: Throwable => Right(e.getClass())
    }
  }

  private def safeCall(fnr: ResultClosure, rfn: RFn) =
    try {
      Left(fnr(rfn))
    } catch {
      case e: Throwable => Right(e.getClass())
    }

  //TODO This is awkward, there is the issue of what if some of the constraints come to different conclusions. Ah but here is where we use cleverness and 
  //do a good partitioning.
  /**
   * p is the parent node. c is the constrant being added
   *  This is asking whether the parameters in p come to the same conclusion as c
   */
  private def resultsSame(l: Code, c: C): Boolean = {
    val resultFromConstraint = evaluateResultForConstraint(c, c.params)
    val result = l.constraints match {
      case (lc :: tail) =>
        val resultFromRoot = evaluateResultForConstraint(c, lc.params)
        val resultDifferent = resultFromConstraint == resultFromRoot
        resultDifferent
      case _ => //so I don't have a constraint. But I have a code. 
        val resultFromRoot= makeClosureForResult(c.params)(l.rfn);
        val resultDifferent = resultFromConstraint == resultFromRoot
        resultDifferent
    }
    result
  }

  private def withConstraint(parent: Option[N], n: RorN, c: C, parentWasTrue: Boolean): RorN =
    try {
      val result: RorN = n match {
        case Left(l: Code) =>
          val cd: CodeFn[B, RFn, R] = c.actualCode
          val newCd = cd.copy(constraints = c :: cd.constraints)
          c.because match {
            case Some(b) => //ok so we are the 
              parent match {
                case Some(p) =>
                  val wouldBreakExisting = parentWasTrue & makeClosureForBecause(p.inputs)(b.because)
                  if (wouldBreakExisting)
                    throw new ConstraintConflictException("Cannot differentiate between\nExisting: " + p.constraintThatCausedNode.description + "\nBeingAdded: " + c.description + "\n\nDetails existing:\n" + p + "\nConstraint:\n" + c)
                  //            Left(newCd)
                  Right(Node(b, c.params, Left(newCd), Left(l), c)) //Adding to the 'yes' of the current
                case _ => //No parent so we are the root.
                  resultsSame(l, c) match {
                    case true => Left(newCd); // we come to same conclusion as root, so we just add ourselves as assertion
                    case false => Right(Node(b, c.params, Left(newCd), Left(l), c)) //So we are if b then new result else default 
                  }
              }
            case None => {
              if (c.expected.isEmpty)
                throw new NoExpectedException("No expected in " + c)
              val actualResultIfUseThisNodesCode = evaluateResultForConstraint(c, c.params);
              actualResultIfUseThisNodesCode match {
                case Right(e: Throwable) => throw e;
                case Left(result) if result == c.expected.get => Left(l.copy(constraints = c :: l.constraints))
                case Left(result) =>
                  throw new AssertionException("Actual Result: " + result + "\nExpected: " + c.expected.get)
              }
            }
          }

        case Right(r) =>
          evaluateBecauseForConstraint(r.constraintThatCausedNode, c.params) match {
            case true => Right(r.copy(yes = withConstraint(Some(r), r.yes, c, true)));
            case false => Right(r.copy(no = withConstraint(Some(r), r.no, c, false)));
          }
      }
      result
    } catch {
      case e: EngineException => throw e;
      case e: Throwable => throw new ExceptionAddingConstraint("Constraint: " + c.description + "\nFull Details:\n" + c, e)
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

//TODO Very unhappy with EngineTest. It's a global variable. I don't know how else to interact with Junit though
object EngineTest {
  def testing = _testing
  private var _testing = false

  var exceptions: Map[Any, Throwable] = Map()

  def test[T](x: () => T) = {
    _testing = true;
    try {
      x()
    } finally
      _testing = false
  }

}

trait Engine[R] extends BuildEngine[R] with EvaluateEngine[R] {
  def defaultRoot: RorN
  def useCases: List[UseCase[B, RFn, R]];
  val constraints: List[C] = useCases.flatMap(_.scenarios)

  val root: RorN = buildRoot(defaultRoot, constraints)

  def buildRoot(root: RorN, constraints: List[C]): RorN = {
    constraints match {
      case c :: rest =>
        try {
          c.configure
          validateBecause(c)
          val newRoot = buildFromConstraints(root, constraints);
          validateConstraint(newRoot, c)
          if (!EngineTest.testing)
            checkConstraint(newRoot, c) //check later
          newRoot

        } catch {
          case e: Throwable if EngineTest.testing =>
            EngineTest.exceptions = EngineTest.exceptions + (c -> e); root
          case e: EngineException => throw e;
          case e: Throwable => throw e
        }
      case _ => root
    }
  }

  def applyParam(root: RorN, params: List[Any]): R = {
    val bec = makeClosureForBecause(params)
    val rfn = evaluate(bec, root)
    makeClosureForResult(params)(rfn)
  }

  private def validateBecause(c: C) {
    c.because match {
      case Some(b) =>
        if (!makeClosureForBecause(c.params).apply(b.because))
          throw new ConstraintBecauseException(c.becauseString + " is not true for " + c.params);
      case None =>
    }
  }

  private def validateConstraint(root: RorN, c: C) {
    c.configure
    val bec = makeClosureForBecause(c.params)
    val rFn: RFn = evaluate(bec, root)
    val actualFromConstraint: R = makeClosureForResult(c.params)(rFn)
    if (c.expected.isEmpty)
      throw new NoExpectedException("No 'produces' in " + c)
    if (actualFromConstraint != c.expected.get)
      throw new ConstraintResultException("Wrong result for " + c.actualCode.description + " for " + c.params + "\nActual: " + actualFromConstraint + "\nExpected: " + c.expected + "\nEngine:\n" + this);
  }

  private def checkConstraint(root: RorN, c: C) {
    validateBecause(c);
    validateConstraint(root, c);
    val actualFromEngine = applyParam(root, c.params);
    if (actualFromEngine != c.expected.get)
      throw new EngineResultException("Wrong result for " + c.actualCode.description + " for " + c.params + "\nActual: " + actualFromEngine + "\nExpected: " + c.expected);
  }

  def validateConstraints {
    for (c <- constraints)
      validateConstraint(root, c)
  }

}

trait EngineToString[R] extends EngineTypesWithRoot[R] {

  def toString(indent: String, root: RorN): String = {
    root match {
      case Left(result) => indent + result.pretty + "\n"
      case Right(node) =>
        indent + "if(" + node.because.pretty + ")\n" +
          toString(indent + " ", node.yes) +
          indent + "else\n" +
          toString(indent + " ", node.no)
    }
  }
  override def toString: String = toString("", root)
}

trait Engine1Types[P, R] extends EngineTypes[R] {
  type B = (P) => Boolean
  type RFn = (P) => R
  def makeClosureForBecause(params: List[Any]) = (b: B) => b(params(0).asInstanceOf[P])
  def makeClosureForResult(params: List[Any]) = (r: RFn) => r(params(0).asInstanceOf[P])
}

trait Engine2Types[P1, P2, R] extends EngineTypes[R] {
  type B = (P1, P2) => Boolean
  type RFn = (P1, P2) => R
  def makeClosureForBecause(params: List[Any]) = (b: B) => b(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2])
  def makeClosureForResult(params: List[Any]) = (r: RFn) => r(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2])
}

trait Engine3Types[P1, P2, P3, R] extends EngineTypes[R] {
  type B = (P1, P2, P3) => Boolean
  type RFn = (P1, P2, P3) => R
  def makeClosureForBecause(params: List[Any]) = (b: B) => b(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[P3])
  def makeClosureForResult(params: List[Any]) = (r: RFn) => r(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[P3])
}

class Engine1[P, R](val default: CodeFn[(P) => Boolean, (P) => R, R], val useCases: List[UseCase[(P) => Boolean, (P) => R, R]]) extends Engine[R] with EngineToString[R] with Function[P, R] with Engine1Types[P, R] {
  def defaultRoot: RorN = Left(default)
  def apply(p: P) = evaluate((b) => b(p), root)(p)
}

class Engine2[P1, P2, R](val default: CodeFn[(P1, P2) => Boolean, (P1, P2) => R, R], val useCases: List[UseCase[(P1, P2) => Boolean, (P1, P2) => R, R]]) extends Engine[R] with EngineToString[R] with Function2[P1, P2, R] with Engine2Types[P1, P2, R] {
  def defaultRoot: RorN = Left(default)
  def apply(p1: P1, p2: P2) = evaluate((b) => b(p1, p2), root)(p1, p2)
}

class Engine3[P1, P2, P3, R](val default: CodeFn[(P1, P2, P3) => Boolean, (P1, P2, P3) => R, R], val useCases: List[UseCase[(P1, P2, P3) => Boolean, (P1, P2, P3) => R, R]]) extends Engine[R] with EngineToString[R] with Function3[P1, P2, P3, R] with Engine3Types[P1, P2, P3, R] {
  def defaultRoot: RorN = Left(default)
  def apply(p1: P1, p2: P2, p3: P3) = evaluate((b) => b(p1, p2, p3), root)(p1, p2, p3)
}

object Engine1 {
  def apply_impl[P: c.WeakTypeTag, R: c.WeakTypeTag](c: Context)(default: c.Expr[(P) => R], useCases: c.Expr[List[UseCase[(P) => Boolean, (P) => R, R]]]): c.Expr[Engine1[P, R]] = {
    import c.universe._
    val expr = reify {
      new Engine1[P, R](new CodeFn[(P) => Boolean, (P) => R, R](default.splice, c.literal(show(default.tree)).splice, List()), useCases.splice)
    }
    c.Expr[Engine1[P, R]](expr.tree)
  }
  def apply[P, R](default: CodeFn[(P) => Boolean, (P) => R, R], useCases: UseCase[(P) => Boolean, (P) => R, R]*): Engine1[P, R] = new Engine1(default, useCases.toList)
  def apply[P, R](default: (P) => R, useCases: List[UseCase[(P) => Boolean, (P) => R, R]]): Engine1[P, R] = macro apply_impl[P, R]; //new Engine1[P, R](default, constraints.toList) with Function1[P, R] with EngineToString[R]

}

object Engine2 {
  def apply_impl[P1: c.WeakTypeTag, P2: c.WeakTypeTag, R: c.WeakTypeTag](c: Context)(default: c.Expr[(P1, P2) => R], useCases: c.Expr[List[UseCase[(P1, P2) => Boolean, (P1, P2) => R, R]]]): c.Expr[Engine2[P1, P2, R]] = {
    import c.universe._
    val expr = reify { new Engine2[P1, P2, R](new CodeFn[(P1, P2) => Boolean, (P1, P2) => R, R](default.splice, c.literal(show(default.tree)).splice, List()), useCases.splice) with EngineToString[R] }
    c.Expr[Engine2[P1, P2, R]](expr.tree)
  }
  def apply[P1, P2, R](default: (P1, P2) => R, useCases: List[UseCase[(P1, P2) => Boolean, (P1, P2) => R, R]]) = macro apply_impl[P1, P2, R]; // new Engine2[P1, P2, R](default, constraints.toList) with Function2[P1, P2, R] with EngineToString[R]

}

object Engine3 {
  def apply_impl[P1: c.WeakTypeTag, P2: c.WeakTypeTag, P3: c.WeakTypeTag, R: c.WeakTypeTag](c: Context)(default: c.Expr[(P1, P2, P3) => R], useCases: c.Expr[List[UseCase[(P1, P2, P3) => Boolean, (P1, P2, P3) => R, R]]]): c.Expr[Engine3[P1, P2, P3, R]] = {
    import c.universe._
    try {
      val expr = reify { new Engine3[P1, P2, P3, R](new CodeFn[(P1, P2, P3) => Boolean, (P1, P2, P3) => R, R](default.splice, c.literal(show(default.tree)).splice, List()), useCases.splice) }
      c.Expr[Engine3[P1, P2, P3, R]](expr.tree)
    } catch {
      case e: Throwable => e.printStackTrace(); throw e;
    }
  }

  //  def apply[P1, P2, P3, R](defaultCode: CodeFn[(P1, P2, P3) => Boolean, (P1, P2, P3) => R, R], useCases: UseCase[(P1, P2, P3) => Boolean, (P1, P2, P3) => R, R]*) = new Engine3[P1, P2, P3, R](defaultCode, useCases.toList);
  def apply[P1, P2, P3, R](default: (P1, P2, P3) => R, useCases: List[UseCase[(P1, P2, P3) => Boolean, (P1, P2, P3) => R, R]]) = macro apply_impl[P1, P2, P3, R]; // new Engine2[P1, P2, R](default, constraints.toList) with Function2[P1, P2, R] with EngineToString[R]
}
