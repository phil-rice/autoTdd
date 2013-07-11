package org.autotdd.engine

import scala.language.experimental.macros
import scala.reflect.macros.Context
import java.text.MessageFormat

class NeedUseCaseException extends Exception
class NeedScenarioException extends Exception
class ScenarioBecauseException(msg: String) extends EngineException(msg)
class ScenarioResultException(msg: String) extends EngineException(msg)
class EngineResultException(msg: String) extends EngineException(msg)
class AssertionException(msg: String) extends EngineException(msg)
class CannotHaveBecauseInFirstScenarioException extends EngineException("")

class ExceptionAddingScenario(msg: String, t: Throwable) extends EngineException(msg, t)

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
trait EngineTypes[R] {
  /** A is a function from the parameters of the engine, and the result to a boolean. It checks that some property is true */
  type A
  /** B is a function from the parameters of the engine to a boolean. It is effectively in calculating which scenario is to be used */
  type B
  /** RFn is a function from the parameters of the engine to a result R. It is used to calculate the result of the engine */
  type RFn
  type ROrException[R] = Either[R, Class[_]]
  type RFnMaker = (Either[Exception, R]) => RFn

  /** this is a function from the parameters to Unit e,g, (P1,P2,P3)=> Unit */
  type CfgFn;

  type RealScenarioBuilder
  /** In order to call an A in the building code, when we don't know anything about the arity, we create a closure holding the parameters and reusltand pass the A function to it */
  type AssertionClosure = (A) => Boolean
  /** In order to call a B in the building code, when we don't know anything about the arity, we create a closure holding the parameters and pass the B function to it */
  type BecauseClosure = (B) => Boolean
  /** In order to call a RFN in the building code, when we don't know anything about the arity, we create a closure holding the parameters and pass the B function to it */
  type ResultClosure = (RFn) => R
  type CfgClosure = (CfgFn) => Unit

  /** This is just a type synonym to save messy code */
  type Code = CodeFn[B, RFn, R]

  def makeClosureForAssertion(params: List[Any], r: R): AssertionClosure
  def makeClosureForBecause(params: List[Any]): BecauseClosure
  def makeClosureForResult(params: List[Any]): ResultClosure
  def makeClosureForCfg(params: List[Any]): CfgClosure
}

trait EngineUniverse[R] extends EngineTypes[R] {
  class ScenarioConflictException(msg: String, val scenarioBeingAdded: Scenario, cause: Throwable) extends EngineException(msg, cause)

  def rfnMaker: (Either[Exception, R]) => RFn
  def logger: TddLogger

  type RorN = Either[CodeAndScenarios, Node]
  type RealScenarioBuilder <: ScenarioBuilder

  def builder: RealScenarioBuilder

  object CodeAndScenarios {
    implicit def delegate_to[B, RFn, R](c: CodeAndScenarios) = c.code
  }

  case class CodeAndScenarios(val code: CodeFn[B, RFn, R], val scenarios: List[Scenario] = List()) {
    def firstAddedScenario = scenarios.last
    override def toString() = getClass.getSimpleName + "(" + code + ":" + scenarios.map(_.description).mkString(",") + ")";
  }

  case class Node(val because: Because[B], inputs: List[Any], yes: RorN, no: RorN, scenarioThatCausedNode: Scenario) {
    def allScenarios = scenarios(Right(this))
    private def scenarios(rOrN: RorN): List[Scenario] = {
      rOrN match {
        case Left(cd) => cd.scenarios
        case Right(n) => scenarios(n.yes) ++ scenarios(n.no)
      }
    }
    override def toString = getClass().getSimpleName() + "(" + because.description + " => " + yes.toString() + " =/> " + no.toString() + " / " + scenarioThatCausedNode.description + ")";
  }

  case class Scenario(
    description: String,
    params: List[Any],
    expected: Option[R] = None,
    code: Option[CodeFn[B, RFn, R]] = None,
    because: Option[Because[B]] = None,
    assertions: List[Assertion[A]] = List(),
    configuration: Option[CfgFn] = None) {

    def configure =
      if (configuration.isDefined) makeClosureForCfg(params)(configuration.get)
    lazy val actualCode: CodeFn[B, RFn, R] = code.getOrElse({
      //    println("Expected: " + expected)
      //    println("Constraint: " + this)
      expected match {
        case Some(e) => new CodeFn(rfnMaker(Right(e)), e.toString);
        case _ => new CodeFn(rfnMaker(Left(new IllegalStateException("Do not have code or expected  for this scenario: " + this))), "No expected or Code")
      }
    })
    def becauseString = because match { case Some(b) => b.description; case _ => "" }

  }
  val scenarioLens = Lens[RealScenarioBuilder, Scenario](
    (b) => b.useCases match {
      case u :: ut => u.scenarios match {
        case s :: st => s
        case _ => throw new NeedScenarioException
      }
      case _ => throw new NeedUseCaseException
    },
    (b, s) => b.useCases match {
      case u :: ut => u.scenarios match {
        case sold :: st => b.withCases(u.copy(scenarios = s :: st) :: ut)
        case _ => throw new NeedScenarioException
      }
      case _ => throw new NeedUseCaseException
    })
  case class UseCase(description: String, scenarios: List[Scenario]);

  trait ScenarioBuilder {
    def useCases: List[UseCase]
    def withCases(useCases: List[UseCase]): RealScenarioBuilder;
    def thisAsBuilder: RealScenarioBuilder
    def because(b: Because[B]) = scenarioLens.mod(thisAsBuilder, (s) => s.copy(because = Some(b)))
    def useCase(description: String) = withCases(UseCase(description, List()) :: useCases);
    def expected(e: R) = scenarioLens.mod(thisAsBuilder, (s) => s.copy(expected = Some(e)))
    def code(c: CodeFn[B, RFn, R]) = scenarioLens.mod(thisAsBuilder, (s) => s.copy(code = Some(c)))
    def configuration[K](cfg: CfgFn) = scenarioLens.mod(thisAsBuilder, (s) => s.copy(configuration = Some(cfg)))
    def assertion(a: Assertion[A]) = scenarioLens.mod(thisAsBuilder, (s) => s.copy(assertions = a :: s.assertions))

    def useCasesForBuild: List[UseCase] =
      useCases.map(u => UseCase(u.description,
        u.scenarios.reverse.zipWithIndex.collect {
          case (s, i) => s.copy(description = u.description + "[" + i + "]")
        })).reverse;

    protected def newScenario(params: List[Any]) =
      useCases match {
        case h :: t => withCases(UseCase(h.description, Scenario("", params) :: h.scenarios) :: t)
        case _ => throw new NeedUseCaseException
      }

  }

  trait EvaluateEngine {

    def evaluate(fn: BecauseClosure, n: RorN, log: Boolean = true): RFn = {
      val result = n match {
        case Left(r) =>
          val result = r.code.rfn
          result
        case Right(n) => evaluate(fn, n, log)
      }
      result
    }

    private def evaluate(fn: BecauseClosure, n: Node, log: Boolean): RFn = {
      val condition = fn(n.because.because)
      if (log)
        logger.infoRun(" Condition" + n.because + " was " + condition)
      condition match {
        case false => evaluate(fn, n.no, log);
        case true => evaluate(fn, n.yes, log);
      }
    }
  }
  trait EngineToString {
    def root: RorN
    def buildRoot(root: RorN, scenarios: List[Scenario]): RorN;

    def toString(indent: String, root: RorN): String = {
      root match {
        case null => indent + "null"
        case Left(result) => indent + result.pretty + "\n"
        case Right(node) =>
          indent + "if(" + node.because.pretty + ")\n" +
            toString(indent + " ", node.yes) +
            indent + "else\n" +
            toString(indent + " ", node.no)
      }
    }
    override def toString(): String = toString("", root)

    def toStringWithScenarios(): String = toStringWithScenarios("", root);

    def increasingScenariosList(cs: List[Scenario]): List[List[Scenario]] =
      cs.foldLeft(List[List[Scenario]]())((a, c) => (a match {
        case (h :: t) => (c :: a.head) :: a;
        case _ => List(List(c))
      }))

    def toStringWithScenarios(indent: String, root: RorN): String = {
      root match {
        case Left(result) =>
          indent + result.pretty + ":" + result.scenarios.map((c) => c.description).mkString(",") + "\n"
        case Right(node) =>
          indent + "if(" + node.because.pretty + ")\n" +
            toStringWithScenarios(indent + " ", node.yes) +
            indent + "else\n" +
            toStringWithScenarios(indent + " ", node.no)
      }
    }

    def constructionString(root: RorN, cs: List[Scenario]) =
      increasingScenariosList(cs).reverse.map((cs) =>
        try {
          val r = buildRoot(root, cs.reverse)
          toStringWithScenarios("", r)
        } catch {
          case e: Throwable => e.getClass() + "\n" + e.getMessage()
        }).mkString("\n")
  }

  trait BuildEngine extends EvaluateEngine with EngineToString {
    protected def validateBecause(c: Scenario) {
      c.configure
      c.because match {
        case Some(b) =>
          if (!makeClosureForBecause(c.params).apply(b.because))
            throw new ScenarioBecauseException(c.becauseString + " is not true for " + c.params);
        case None =>
      }
    }

    protected def validateScenario(root: RorN, c: Scenario) {
      c.configure
      val bec = makeClosureForBecause(c.params)
      val rFn: RFn = evaluate(bec, root, false)
      val actualFromScenario: R = makeClosureForResult(c.params)(rFn)
      if (c.expected.isEmpty)
        throw new NoExpectedException("No 'produces' in " + c)
      if (actualFromScenario != c.expected.get)
        throw new ScenarioResultException("Wrong result for " + c.actualCode.description + " for " + c.params + "\nActual: " + actualFromScenario + "\nExpected: " + c.expected + "\nRoot:\n" + toString("", root));
      val assertionClosure = makeClosureForAssertion(c.params, actualFromScenario);
      for (a <- c.assertions) {
        val result = assertionClosure(a.assertion)
        if (!result)
          throw new AssertionException("\nAssertion " + a.description + " failed.\nParams are " + c.params + "\nResult was " + actualFromScenario)
      }

    }

    def buildFromScenarios(root: RorN, cs: List[Scenario]): RorN = {
      cs match {
        case c :: tail =>
          validateBecause(c);
          //                  validateScenario(root, c);
          buildFromScenarios(withScenario(None, root, c, true), tail)

        case _ => root;
      }
    }

    def validateScenarios(root: RorN, cs: List[Scenario]) {
      for (c <- cs) {
        c.configure
        val bc = makeClosureForBecause(c.params)
        val fnr = makeClosureForResult(c.params)
        val resultFn: RFn = evaluate(bc, root, false);
        val result = fnr(resultFn)
        val fna = makeClosureForAssertion(c.params, result)
        for (a <- c.assertions) {
          if (!fna(a.assertion))
            throw new AssertionException("\nAssertion " + a.description + " failed.\nParams are " + c.params + "\nResult was " + result)
        }
      }
    }

    def evaluateBecauseForScenario(c: Scenario, params: List[Any]) = {
      val fn = makeClosureForBecause(params)
      //    c.configure
      c.because match { case Some(b) => fn(b.because); case _ => throw new IllegalStateException("No because in " + c) }
    }

    def evaluateResultForScenario(c: Scenario, params: List[Any]): ROrException[R] = {
      val fnr = makeClosureForResult(params)
      c.configure
      try {
        val result = Left(fnr(c.actualCode.rfn));
        result
      } catch {
        case e: Throwable => Right(e.getClass())
      }
    }

    private def safeCall(fnr: ResultClosure, rfn: RFn): ROrException[R] =
      try {
        Left(fnr(rfn))
      } catch {
        case e: Throwable => Right(e.getClass())
      }

    //TODO This is awkward, there is the issue of what if some of the scenarios come to different conclusions. Ah but here is where we use cleverness and 
    //do a good partitioning.
    /**
     * p is the parent node. c is the constrant being added
     *  This is asking whether the parameters in p come to the same conclusion as c
     */
    private def resultsSame(l: CodeAndScenarios, c: Scenario): Boolean = {
      val resultFromScenario: ROrException[R] = evaluateResultForScenario(c, c.params)
      val result = l.scenarios match {
        case (lc :: tail) =>
          val resultFromRoot: ROrException[R] = evaluateResultForScenario(lc, c.params)
          val resultSame = resultFromScenario == resultFromRoot
          resultSame
        case _ => //so I don't have a scenario. But I have a code. 
          c.configure
          val resultFromRoot = makeClosureForResult(c.params)(l.rfn);
          val resultSame = resultFromScenario == Left(resultFromRoot)
          resultSame
      }
      result
    }

    private def withScenario(parent: Option[Node], n: RorN, c: Scenario, parentWasTrue: Boolean): RorN =
      try {
        println("Scenario: " + c)
        val result: RorN = n match {
          case null =>
            if (c.because.isDefined)
              throw new CannotHaveBecauseInFirstScenarioException
            logger.debugCompile("Adding " + c.description + " as new root")
            Left(CodeAndScenarios(c.actualCode, List(c)))
          case Left(l: CodeAndScenarios) =>
            val cd: CodeFn[B, RFn, R] = c.actualCode
            val newCnC = CodeAndScenarios(c.actualCode, List(c))
            c.because match {
              case Some(b) =>
                parent match {
                  case Some(p) =>
                    p.scenarioThatCausedNode.configure
                    val wouldBreakExisting = parentWasTrue & makeClosureForBecause(p.inputs)(b.because)
                    if (wouldBreakExisting) {
                      val existing = p.scenarioThatCausedNode.description + " " + p.scenarioThatCausedNode.params;
                      val existingScenario = p.scenarioThatCausedNode
                      c.configure;
                      throw new ScenarioConflictException("Cannot differentiate between\nExisting: " + existing +
                        "\nBeingAdded: " + c.description + " " + c.params +
                        "\n\nDetails existing:\n" + existingScenario + "\nScenario:\n" + c, c, null)
                    }
                    //                  logger.debugCompile( "Adding " + newCnC + "to yes of leaf " + parent.toString)
                    logger.debugCompile("Adding " + c.description + " under " + (if (parentWasTrue) "yes" else "no") + " of node " + p.scenarioThatCausedNode.description)
                    Right(Node(b, c.params, Left(newCnC), Left(l), c)) //Adding to the 'yes' of the current
                  case _ => //No parent so we are the root.
                    resultsSame(l, c) match {
                      case true =>
                        logger.debugCompile("Adding " + c.description + " as extra scenario for root")
                        Left(l.copy(scenarios = c :: l.scenarios)); // we come to same conclusion as root, so we just add ourselves as assertion
                      case false =>
                        logger.debugCompile("Adding " + c.description + " as first if then else")
                        Right(Node(b, c.params, Left(newCnC), Left(l), c)) //So we are if b then new result else default 
                    }
                }
              case None => {
                if (c.expected.isEmpty)
                  throw new NoExpectedException("No expected in " + c)
                val actualResultIfUseThisScenariosCode: ROrException[R] = safeCall(makeClosureForResult(c.params), l.code.rfn);
                actualResultIfUseThisScenariosCode match {
                  case Right(e: Throwable) => throw e;
                  case Left(result) if result == c.expected.get =>
                    parentWasTrue match {
                      case true =>
                        if (actualResultIfUseThisScenariosCode == Left(c.expected.get)) {
                          logger.debugCompile("Adding " + c.description + " as extra scenario for " + l)
                          Left(l.copy(scenarios = c :: l.scenarios))
                        } else
                          throw new AssertionException("Adding assertion and got wrong value.\nAdding: " + c.description + "\nDetails: " + c + "\nto: " + l + "\nActual result: " + actualResultIfUseThisScenariosCode)
                      case false => //Only way here is when I am coming down a no node. By definition I have a parent and it was false
                        if (parent.isDefined && resultsSame(parent.get.no.left.get, c)) {
                          logger.debugCompile("Adding " + c.description + " as extra scenario for " + l.code)
                          Left(l.copy(scenarios = c :: l.scenarios))
                        } else
                          throw new AssertionException("Adding assertion to else and got wrong value.\nAdding: " + c + "\nto: " + parent.get.no + "\n")
                    }
                  case Left(result) =>
                    throw new AssertionException("Actual Result: " + result + "\nExpected: " + c.expected.get)
                }
              }
            }

          case Right(r) =>
            evaluateBecauseForScenario(r.scenarioThatCausedNode, c.params) match {
              case true => Right(r.copy(yes = withScenario(Some(r), r.yes, c, true)));
              case false => Right(r.copy(no = withScenario(Some(r), r.no, c, false)));
            }
        }
        //          println("Produced: " + result)
        result
      } catch {
        case e: EngineException => throw e;
        case e: Throwable => throw new ExceptionAddingScenario("Scenario: " + c.description + "\nFull Details:\n" + c, e)
      }
    private def findLastMatch(fn: BecauseClosure, root: Option[Node], lastMatch: Option[Node], params: List[Any]): Option[Node] = {
      root match {
        case None => None
        case Some(r) =>
          fn(r.because.because) match {
            case true => findLastMatch(fn, r.yes, root, params)
            case false => findLastMatch(fn, r.no, lastMatch, params)
          }
      }
    }
    private def findLastMatch(fn: BecauseClosure, root: RorN, lastMatch: Option[Node], params: List[Any]): Option[Node] = {
      root match {
        case Left(r) => lastMatch
        case Right(n) => findLastMatch(fn, Some(n), lastMatch, params)
      }
    }
  }

  trait Engine extends BuildEngine {
    def defaultRoot: RorN
    def useCases: List[UseCase];
    def scenarios: List[Scenario] = useCases.flatMap(_.scenarios)

    val root: RorN = buildRoot(defaultRoot, scenarios)
    validateScenarios(root, scenarios)

    def constructionString: String = constructionString(defaultRoot, scenarios)
    def logParams(p: Any*) =
      logger.debugRun("Executing " + p.map(logger).mkString(","))

    def logResult(fn: => R): R = {
      val result: R = fn;
      logger.debugRun(" Result " + logger(result))
      result
    }

    def buildRoot(root: RorN, scenarios: List[Scenario]): RorN = {
      scenarios match {
        case c :: rest =>
          try {
            c.configure
            validateBecause(c)
            val newRoot = buildFromScenarios(root, scenarios);
            validateScenario(newRoot, c)
            if (!EngineTest.testing)
              checkScenario(newRoot, c) //check later
            newRoot

          } catch {

            case e: ScenarioConflictException =>
              val index = scenarios.indexOf(e.scenarioBeingAdded)
              val safeCs = scenarios.take(index)
              val msg = constructionString(root, safeCs) + "\n" + e.getMessage()
              throw new ScenarioConflictException(msg, e.scenarioBeingAdded, e)

            case e: Throwable if EngineTest.testing =>
              EngineTest.exceptions = EngineTest.exceptions + (c -> e); root
            case e: EngineException =>
              throw e;
            case e: Throwable =>
              throw e
          }
        case _ => root
      }

    }

    def applyParam(root: RorN, params: List[Any], log: Boolean): R = {
      val bec = makeClosureForBecause(params)
      val rfn = evaluate(bec, root, log)
      makeClosureForResult(params)(rfn)
    }

    private def checkScenario(root: RorN, c: Scenario) {
      validateBecause(c);
      validateScenario(root, c);
      val actualFromEngine = applyParam(root, c.params, false);
      if (actualFromEngine != c.expected.get)
        throw new EngineResultException("Wrong result for " + c.actualCode.description + " for " + c.params + "\nActual: " + actualFromEngine + "\nExpected: " + c.expected);
    }

    def validateScenarios {
      for (c <- scenarios)
        validateScenario(root, c)
    }

  }

}
trait Engine1Types[P, R] extends EngineTypes[R] {
  type A = (P, R) => Boolean
  type B = (P) => Boolean
  type RFn = (P) => R
  type CfgFn = (P) => Unit
  def rfnMaker = RfnMaker.rfn1ConstantMaker[P, R]
  def makeClosureForBecause(params: List[Any]) = (b: B) => b(params(0).asInstanceOf[P])
  def makeClosureForResult(params: List[Any]) = (r: RFn) => r(params(0).asInstanceOf[P])
  def makeClosureForCfg(params: List[Any]) = (c: CfgFn) => c(params(0).asInstanceOf[P])
  def makeClosureForAssertion(params: List[Any], r: R) = (a: A) => a(params(0).asInstanceOf[P], r);
}

trait Engine2Types[P1, P2, R] extends EngineTypes[R] {
  type A = (P1, P2, R) => Boolean
  type B = (P1, P2) => Boolean
  type RFn = (P1, P2) => R
  type CfgFn = (P1, P2) => Unit
  def rfnMaker = RfnMaker.rfn2ConstantMaker[P1, P2, R]
  def makeClosureForBecause(params: List[Any]) = (b: B) => b(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2])
  def makeClosureForResult(params: List[Any]) = (r: RFn) => r(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2])
  def makeClosureForCfg(params: List[Any]) = (c: CfgFn) => c(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2])
  def makeClosureForAssertion(params: List[Any], r: R) = (a: A) => a(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], r);
}

trait Engine3Types[P1, P2, P3, R] extends EngineTypes[R] {
  type A = (P1, P2, P3, R) => Boolean
  type B = (P1, P2, P3) => Boolean
  type RFn = (P1, P2, P3) => R
  type CfgFn = (P1, P2, P3) => Unit
  def rfnMaker = RfnMaker.rfn3ConstantMaker[P1, P2, P3, R]
  def makeClosureForBecause(params: List[Any]) = (b: B) => b(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[P3])
  def makeClosureForResult(params: List[Any]) = (r: RFn) => r(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[P3])
  def makeClosureForCfg(params: List[Any]) = (c: CfgFn) => c(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[P3])
  def makeClosureForAssertion(params: List[Any], r: R) = (a: A) => a(params(0).asInstanceOf[P1], params(1).asInstanceOf[P2], params(2).asInstanceOf[P3], r);
}

class Engine

object Engine {
  def apply[P, R]() = new BuilderFactory1[P, R]().builder
  def apply[P1, P2, R]() = new BuilderFactory2[P1, P2, R]().builder
  def apply[P1, P2, P3, R]() = new BuilderFactory3[P1, P2, P3, R]().builder
}

class BuilderFactory1[P, R](override val logger: TddLogger = TddLogger.noLogger) extends EngineUniverse[R] with Engine1Types[P, R] {
  type RealScenarioBuilder = Builder1

  def builder = new Builder1

  class Builder1(val useCases: List[UseCase] = List()) extends ScenarioBuilder {
    def thisAsBuilder = this
    def withCases(useCases: List[UseCase]) = new Builder1(useCases)
    def scenario(p: P) = newScenario(List(p))
    def build = new Engine with Function[P, R] {
      def useCases = useCasesForBuild
      def defaultRoot = null
      def apply(p: P): R = {
        logParams(p)
        val rfn: RFn = evaluate((b) => b(p), root);
        val result: R = rfn(p)
        logResult(result)
      }
      override def toString() = toStringWithScenarios
    }
  }
}
class BuilderFactory2[P1, P2, R](override val logger: TddLogger = TddLogger.noLogger) extends EngineUniverse[R] with Engine2Types[P1, P2, R] {
  type RealScenarioBuilder = Builder2
  def builder = new Builder2
  class Builder2(val useCases: List[UseCase] = List()) extends ScenarioBuilder {
    def thisAsBuilder = this
    def withCases(useCases: List[UseCase]) = new Builder2(useCases)
    def scenario(p1: P1, p2: P2) = newScenario(List(p1, p2))
    def build = new Engine with Function2[P1, P2, R] {
      def useCases = useCasesForBuild
      def defaultRoot = null
      def apply(p1: P1, p2: P2): R = {
        logParams(p1, p2)
        val rfn: RFn = evaluate((b) => b(p1, p2), root);
        val result: R = rfn(p1, p2)
        logResult(result)
      }
      override def toString() = toStringWithScenarios
    }
  }
}
class BuilderFactory3[P1, P2, P3, R](override val logger: TddLogger = TddLogger.noLogger) extends EngineUniverse[R] with Engine3Types[P1, P2, P3, R] {

  type RealScenarioBuilder = Builder3
  def builder = new Builder3
  class Builder3(val useCases: List[UseCase] = List()) extends ScenarioBuilder {
    def thisAsBuilder = this
    def withCases(useCases: List[UseCase]) = new Builder3(useCases)
    def scenario(p1: P1, p2: P2, p3: P3) = newScenario(List(p1, p2, p3))
    def build = new Engine with Function3[P1, P2, P3, R] {
      def useCases = useCasesForBuild
      def defaultRoot = null
      def apply(p1: P1, p2: P2, p3: P3): R = {
        logParams(p1, p2)
        val rfn: RFn = evaluate((b) => b(p1, p2, p3), root);
        val result: R = rfn(p1, p2, p3)
        logResult(result)
      }
      override def toString() = toStringWithScenarios
    }
  }
}
trait NodeComparator[R] extends EngineUniverse[R] {

  def compareNodes(n1: RorN, n2: RorN): List[String] =
    compareNodes("", n1, n2)

  def compareNodes(prefix: String, n1: RorN, n2: RorN): List[String] = {
    n1 match {
      case Left(result1) =>
        n2 match {
          case Left(result2) => compareResults(prefix, result1, result2)
          case Right(node2) => List(prefix + "left is result " + result1.description + " Right is tree " + node2)
        }
      case Right(node1) =>
        n2 match {
          case Left(result2) => List(prefix + "left is tree " + node1 + " Right is result " + result2.description)
          case Right(node2) => compareNodes(prefix, node1, node2)
        }
    }
  }
  def compareResults(prefix: String, c1: CodeAndScenarios, c2: CodeAndScenarios): List[String] = {
    check(prefix + "result {0} {1}", c1.description, c2.description) ++
      compareConstraints(prefix + "scenarios/", c1.scenarios, c2.scenarios)

  }
  def compareNodes(prefix: String, n1: Node, n2: Node): List[String] = {
    check(prefix + "because {0} {1}", n1.because.description, n2.because.description) ++
      check(prefix + "inputs {0} {1}", n1.inputs, n2.inputs) ++
      compareNodes(prefix + "yes/", n1.yes, n2.yes) ++
      compareNodes(prefix + "no/", n1.no, n2.no)
  }

  def compareConstraints(prefix: String, c1s: List[Scenario], c2s: List[Scenario]): List[String] = {
    val sizeMismatch = c1s.size != c2s.size match { case true => List(prefix + " sizes " + c1s.size + "," + c2s.size); case _ => List() };
    sizeMismatch ++ (c1s, c2s).zipped.flatMap((c1, c2) => c1 != c2 match { case true => compareConstraint(prefix + "[" + c1.becauseString + "]", c1, c2); case _ => List() });
  }

  def compareConstraint(prefix: String, c1: Scenario, c2: Scenario): List[String] = {
    val b = c1.becauseString != c2.becauseString match { case true => List(prefix + "because " + c1.becauseString + ", " + c2.becauseString); case _ => List() }
    val i = c1.params != c2.params match { case true => List(prefix + "params " + c1.params + ", " + c2.params); case _ => List() }
    val e = c1.expected != c2.expected match { case true => List(prefix + "expected " + c1.expected + ", " + c2.expected); case _ => List() }
    val c = c1.actualCode.description != c2.actualCode.description match { case true => List(prefix + "code " + c1.actualCode.description + ", " + c2.actualCode.description); case _ => List() }
    b ++ i ++ e ++ c
  }
  def compareSize(prefix: String, c1s: List[Scenario], c2s: List[Scenario]): List[String] = {
    if (c1s.size != c2s.size)
      List(prefix + " sizes " + c1s.size + "," + c2s.size);
    else
      List()
  }

  def check[T <: AnyRef](pattern: String, t1: T, t2: T): List[String] = {
    if (t1 == t2)
      List()
    else
      List(MessageFormat.format(pattern, t1, t2))
  }

}
