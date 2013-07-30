package org.autotdd.engine

import scala.language.experimental.macros
import scala.reflect.macros.Context
import java.text.MessageFormat

class NeedUseCaseException extends Exception
class NeedScenarioException extends Exception
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
  class ScenarioException(msg: String, val scenario: Scenario, cause: Throwable = null) extends EngineException(msg, cause)

  class EngineResultException(msg: String) extends EngineException(msg)
  class ScenarioBecauseException(msg: String, scenario: Scenario) extends ScenarioException(msg, scenario)
  class ScenarioResultException(msg: String, scenario: Scenario) extends ScenarioException(msg, scenario)
  class AssertionException(msg: String, scenario: Scenario) extends ScenarioException(msg, scenario)
  class ScenarioConflictException(msg: String, scenario: Scenario, cause: Throwable = null) extends ScenarioException(msg, scenario, cause)
  class MultipleExceptions(msg: String, val scenarioExceptionMap: ScenarioExceptionMap) extends EngineException(msg)

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
    description: Option[String],
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

  type ScenarioExceptionMap = Map[Scenario, Throwable]
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

  trait ScenarioBuilder extends ScenarioWalker {
    def useCases: List[UseCase]
    def withCases(useCases: List[UseCase]): RealScenarioBuilder;
    def thisAsBuilder: RealScenarioBuilder
    def because(b: Because[B], comment: String = "") = scenarioLens.mod(thisAsBuilder, (s) => s.copy(because = Some(b.copy(comment = comment))))
    def useCase(description: String) = withCases(UseCase(description, List()) :: useCases);
    def expected(e: R) = scenarioLens.mod(thisAsBuilder, (s) => s.copy(expected = Some(e)))
    def code(c: CodeFn[B, RFn, R], comment: String = "") = scenarioLens.mod(thisAsBuilder, (s) => s.copy(code = Some(c.copy(comment = comment))))
    def configuration[K](cfg: CfgFn) = scenarioLens.mod(thisAsBuilder, (s) => s.copy(configuration = Some(cfg)))
    def assertion(a: Assertion[A], comment: String = "") = scenarioLens.mod(thisAsBuilder, (s) => s.copy(assertions = a.copy(comment = comment) :: s.assertions))

    def useCasesForBuild: List[UseCase] =
      useCases.map(u => UseCase(u.description,
        u.scenarios.reverse.zipWithIndex.collect {
          case (s, i) => s.copy(description = Some(s.description.getOrElse(u.description + "[" + i + "]")))
        })).reverse;

    protected def newScenario(description: String, params: List[Any]) =
      useCases match {
        case h :: t => {
          val descriptionString = if (description == null) None else Some(description);
          withCases(UseCase(h.description, Scenario(descriptionString, params) :: h.scenarios) :: t)
        }
        case _ => throw new NeedUseCaseException
      }

  }
  trait IfThenPrinter {
    def resultPrint: (String, CodeAndScenarios) => String
    def ifPrint: (String, Node) => String
    def elsePrint: (String, Node) => String
    def endPrint: (String, Node) => String
    def titlePrint: (String, Scenario) => String
  }

  trait ScenarioVisitor {
    def start
    def visitUseCase(useCaseindex: Int, u: UseCase)
    def visitScenario(useCaseindex: Int, u: UseCase, scenarioIndex: Int, s: Scenario)
    def visitUseCaseEnd(u: UseCase)
    def end
  }

  trait ScenarioWalker {
    def useCases: List[UseCase];
    def walkScenarios(v: ScenarioVisitor, reverse: Boolean) {
      def actual[X](list: List[X]) = if (reverse) list.reverse else list;
      v.start
      for ((u, ui) <- actual(useCases).zipWithIndex) {
        v.visitUseCase(ui, u)
        for ((s, si) <- actual(u.scenarios).zipWithIndex)
          v.visitScenario(ui, u, si, s)
        v.visitUseCaseEnd(u)
      }
      v.end
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
        logger.evaluating(n.because, condition)
      condition match {
        case false => evaluate(fn, n.no, log);
        case true => evaluate(fn, n.yes, log);
      }
    }
  }
  trait EngineToString {
    def root: RorN
    def buildRoot(root: RorN, scenarios: List[Scenario]): (RorN, ScenarioExceptionMap)

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

    class DefaultIfThenPrinter extends IfThenPrinter {
      def resultPrint = (indent, result) => indent + result.pretty + ":" + result.scenarios.map((s) => s.description.getOrElse("")).mkString(",") + "\n"
      def ifPrint = (indent, node) => indent + "if(" + node.because.pretty + ")\n"
      def elsePrint = (indent, node) => indent + "else\n";
      def endPrint = (indent, node) => "";
      def titlePrint: (String, Scenario) => String = (indent, scenario) => "";
    }

    def toStringWith(indent: String, root: RorN, printer: IfThenPrinter): String = {
      root match {
        case null => "Could not toString as root as null. Possibly because of earlier exceptions"
        case Left(result) => printer.resultPrint(indent, result)
        case Right(node) =>
          val ifString = printer.ifPrint(indent, node)
          val yesString = toStringWith(indent + " ", node.yes, printer)
          val elseString = printer.elsePrint(indent, node)
          val noString = toStringWith(indent + " ", node.no, printer)
          val endString = printer.endPrint(indent, node)
          val result = ifString + yesString + elseString + noString + endString
          return result
      }
    }

    def toStringWithScenarios(indent: String, root: RorN): String =
      toStringWith(indent, root, new DefaultIfThenPrinter())

    def constructionString(root: RorN, cs: List[Scenario], printer: IfThenPrinter) =
      increasingScenariosList(cs).reverse.map((cs) =>
        try {
          val c = cs.head
          val title = printer.titlePrint("", c)
          val (r, s) = buildRoot(root, cs.reverse)
          title + toStringWith("", r, printer)
        } catch {
          case e: Throwable => e.getClass() + "\n" + e.getMessage()
        }).mkString("\n")
  }

  trait BuildEngine extends EvaluateEngine with EngineToString {
    protected def validateBecause(s: Scenario) {
      s.configure
      s.because match {
        case Some(b) =>
          if (!makeClosureForBecause(s.params).apply(b.because))
            throw new ScenarioBecauseException(s.becauseString + " is not true for " + s.params, s);
        case None =>
      }
    }

    protected def validateScenario(root: RorN, s: Scenario) {
      s.configure
      val bec = makeClosureForBecause(s.params)
      val rFn: RFn = evaluate(bec, root, false)
      val actualFromScenario: R = makeClosureForResult(s.params)(rFn)
      if (s.expected.isEmpty)
        throw new NoExpectedException("No 'produces' in " + s)
      if (actualFromScenario != s.expected.get)
        throw new ScenarioResultException("Wrong result for " + s.actualCode.description + " for " + s.params + "\nActual: " + actualFromScenario + "\nExpected: " + s.expected + "\nRoot:\n" + toString("", root), s);
      val assertionClosure = makeClosureForAssertion(s.params, actualFromScenario);
      for (a <- s.assertions) {
        val result = assertionClosure(a.assertion)
        if (!result)
          throw new AssertionException("\nAssertion " + a.description + " failed.\nParams are " + s.params + "\nResult was " + actualFromScenario, s)
      }

    }

    def buildFromScenarios(root: RorN, cs: List[Scenario], seMap: ScenarioExceptionMap): (RorN, ScenarioExceptionMap) = {
      cs match {
        case c :: tail =>
          try {
            validateBecause(c);
            val newRoot = withScenario(None, root, c, true)
            validateScenario(newRoot, c);
            buildFromScenarios(newRoot, tail, seMap)
          } catch {
            case e: ThreadDeath =>
              throw e
            case e: Throwable =>
              buildFromScenarios(root, tail, seMap + (c -> e))
          }

        case _ => (root, seMap);
      }
    }

    def validateScenarios(root: RorN, scenarios: List[Scenario]) {
      if (root == null)
        if (EngineTest.testing)
          return
        else
          throw new NullPointerException("Cannot validate scenario as root doesn't exist")
      for (s <- scenarios) {
        s.configure
        val bc = makeClosureForBecause(s.params)
        val fnr = makeClosureForResult(s.params)
        val resultFn: RFn = evaluate(bc, root, false);
        val result = fnr(resultFn)
        val fna = makeClosureForAssertion(s.params, result)
        for (a <- s.assertions) {
          if (!fna(a.assertion))
            throw new AssertionException("\nAssertion " + a.description + " failed.\nParams are " + s.params + "\nResult was " + result, s)
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

    //TODO This is awkward, there is the issue of what if some of the scenarios come to different conclusions. Ah but here is where in the future we can use cleverness and 
    //do a good partitioning.
    /**
     * p is the parent node. c is the constraint being added
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

    private def withScenario(parent: Option[Node], n: RorN, s: Scenario, parentWasTrue: Boolean): RorN =
      try {
        //        println("Scenario: " + s)
        val result: RorN = n match {
          case null =>
            if (s.because.isDefined)
              throw new CannotHaveBecauseInFirstScenarioException
            logger.newRoot(s.description.get)
            Left(CodeAndScenarios(s.actualCode, List(s)))
          case Left(l: CodeAndScenarios) =>
            val cd: CodeFn[B, RFn, R] = s.actualCode
            val newCnC = CodeAndScenarios(s.actualCode, List(s))
            s.because match {
              case Some(b) =>
                parent match {
                  case Some(p) =>
                    p.scenarioThatCausedNode.configure
                    val wouldBreakExisting = parentWasTrue & makeClosureForBecause(p.inputs)(b.because)
                    if (wouldBreakExisting) {
                      val existing = p.scenarioThatCausedNode.description + " " + p.scenarioThatCausedNode.params;
                      val existingScenario = p.scenarioThatCausedNode
                      s.configure;
                      throw new ScenarioConflictException("Cannot differentiate between\nExisting: " + existing +
                        "\nBeing Added: " + s.description + " " + s.params +
                        "\n\nDetails of Existing Scenario: " + existingScenario + "\n\nDetails of New Scenario: " + s, s, null)
                    }
                    //                  logger.debugCompile( "Adding " + newCnC + "to yes of leaf " + parent.toString)
                    logger.addingUnder(s.description.get, parentWasTrue, p.scenarioThatCausedNode.description.get)
                    Right(Node(b, s.params, Left(newCnC), Left(l), s)) //Adding to the 'yes' of the current
                  case _ => //No parent so we are the root.
                    resultsSame(l, s) match {
                      case true =>
                        logger.addScenarioForRoot(s.description.get)
                        Left(l.copy(scenarios = s :: l.scenarios)); // we come to same conclusion as root, so we just add ourselves as assertion
                      case false =>
                        logger.addFirstIfThenElse(s.description.get)
                        Right(Node(b, s.params, Left(newCnC), Left(l), s)) //So we are if b then new result else default 
                    }
                }
              case None => {
                if (s.expected.isEmpty)
                  throw new NoExpectedException("No expected in " + s)
                val actualResultIfUseThisScenariosCode: ROrException[R] = safeCall(makeClosureForResult(s.params), l.code.rfn);
                actualResultIfUseThisScenariosCode match {
                  case Right(e: Throwable) => throw e;
                  case Left(result) if result == s.expected.get =>
                    parentWasTrue match {
                      case true =>
                        if (actualResultIfUseThisScenariosCode == Left(s.expected.get)) {
                          logger.addScenarioFor(s.description.get, l.code)
                          Left(l.copy(scenarios = s :: l.scenarios))
                        } else
                          throw new AssertionException("Adding assertion and got wrong value.\nAdding: " + s.description + "\nDetails: " + s + "\nto: " + l + "\nActual result: " + actualResultIfUseThisScenariosCode, s)
                      case false => //Only way here is when I am coming down a no node. By definition I have a parent and it was false
                        if (parent.isDefined && resultsSame(parent.get.no.left.get, s)) {
                          logger.addScenarioFor(s.description.get, l.code)
                          Left(l.copy(scenarios = s :: l.scenarios))
                        } else
                          throw new AssertionException("Adding assertion to else and got wrong value.\nAdding: " + s + "\nto: " + parent.get.no + "\n", s)
                    }
                  case Left(result) =>
                    throw new AssertionException("Actual Result: " + result + "\nExpected: " + s.expected.get, s)
                }
              }
            }

          case Right(r) =>
            evaluateBecauseForScenario(r.scenarioThatCausedNode, s.params) match {
              case true => Right(r.copy(yes = withScenario(Some(r), r.yes, s, true)));
              case false => Right(r.copy(no = withScenario(Some(r), r.no, s, false)));
            }
        }
        //          println("Produced: " + result)
        result
      } catch {
        case e: EngineException => throw e;
        case e: Throwable => throw new ExceptionAddingScenario("Scenario: " + s.description + "\nFull Details:\n" + s, e)
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

  trait Engine extends BuildEngine with ScenarioWalker {
    def defaultRoot: RorN
    def useCases: List[UseCase];
    lazy val scenarios: List[Scenario] = useCases.flatMap(_.scenarios)

    private val rootAndExceptionMap = buildRoot(defaultRoot, scenarios)
    val root: RorN = rootAndExceptionMap._1
    val scenarioExceptionMap: ScenarioExceptionMap = rootAndExceptionMap._2

    if (!EngineTest.testing)
      validateScenarios(root, scenarios)

    def constructionString: String =
      constructionString(defaultRoot, scenarios, new DefaultIfThenPrinter {
        override def titlePrint =
          (indent, scenario) =>
            indent + "Adding " + scenario.description + " " + logger(scenario.params) + "\n";
      })
    def logParams(p: Any*) =
      logger.executing(p.toList)

    def logResult(fn: => R): R = {
      val result: R = fn;
      logger.result(result)
      result
    }

    def buildRoot(root: RorN, scenarios: List[Scenario]): (RorN, ScenarioExceptionMap) = {
      scenarios match {
        case s :: rest =>
          val newRootAndExceptionMap = buildFromScenarios(root, scenarios, Map());
          val (newRoot, seMap) = newRootAndExceptionMap
          if (!EngineTest.testing) {
            seMap.size match {
              case 0 => ;
              case 1 => throw seMap.values.head
              case _ =>
                throw new MultipleExceptions(s"Could not build Engine $seMap", seMap)
            }
          }
          newRootAndExceptionMap

        case _ => (root, Map())
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
      println("In validate scenarios")
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
    def scenario(p: P, description: String = null) = newScenario(description, List(p))
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
    def scenario(p1: P1, p2: P2, description: String = null) = newScenario(description, List(p1, p2))
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
    def scenario(p1: P1, p2: P2, p3: P3, description: String = null) = newScenario(description, List(p1, p2, p3))
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
