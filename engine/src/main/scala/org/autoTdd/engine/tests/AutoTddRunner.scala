package org.autotdd.engine.tests

import org.junit.runner.Description
import scala.collection.JavaConversions._
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import org.autotdd.engine._
import scala.reflect.runtime.{ universe => ru }
import java.lang.reflect.Method
import junit.framework.Assert
import org.junit.runner.notification.Failure
import java.io.File
import scala.collection.mutable.StringBuilder
import sys.process._
import java.lang.reflect.Field

object AutoTddRunner {
  val separator = "\n#########\n"
  val userHome = System.getProperty("user.home");
  val directory = new File(userHome, ".autoTdd")

}
trait NotActuallyFactory[R] extends EngineUniverse[R] {
  def builder: RealScenarioBuilder = ???
  def logger: org.autotdd.engine.TddLogger = TddLogger.noLogger
  def rfnMaker: scala.util.Either[Exception, Any] => RFn = ???
  def makeClosureForBecause(params: List[Any]) = ???
  def makeClosureForCfg(params: List[Any]) = ???
  def makeClosureForResult(params: List[Any]) = ???
  def makeClosureForAssertion(params: List[Any], r: R) = ???

}

class AutoTddRunner(val clazz: Class[Any]) extends Runner with JunitUniverse[Any] with NotActuallyFactory[Any] {

  val getDescription = Description.createSuiteDescription("ATDD: " + clazz.getName);

  val instance = EngineTest.test(() => { instantiate(clazz) });

  var engineMap: Map[Description, Engine] = Map()
  var ScenarioMap: Map[Description, Scenario] = Map()
  var exceptionMap: Map[Description, Throwable] = Map()

  def scenarioReporter(f: File) = new JunitScenarioReporter(new JunitFileManipulator(f), logger)

  for (m <- clazz.getDeclaredMethods().filter((m) => returnTypeIsEngine(m))) {
    val engineDescription = Description.createSuiteDescription(m.getName())
    val engine: Engine = m.invoke(instance).asInstanceOf[Engine];
    println(m.getName())
    println(engine)
    add(engineDescription, engine)
  }
  for (f <- clazz.getFields().filter((f) => typeIsEngine(f))) {
    val engineDescription = Description.createSuiteDescription(f.getName())
    val engine: Engine = f.get(instance).asInstanceOf[Engine];
    println(f.getName())
    println(engine)
    add(engineDescription, engine)
  }

  def add(engineDescription: Description, engine: Engine) {
    getDescription.addChild(engineDescription)

    val reporter = (scenarioReporter(fileFor(clazz, engineDescription, "html")))
    engine.walkScenarios(reporter, true);

    engineMap = engineMap + (engineDescription -> engine)
    for (s <- engine.scenarios) {
      val name = s.description.collect { case d: String => d + "/" }.getOrElse("") + s.params.reduce((acc, p) => acc + ", " + p) + " => " + s.expected + " " + s.becauseString
      val cleanedName = name.replace("(", "<").replace(")", ">").replace("\n", "\\n");
      println("   " + cleanedName)
      val ScenarioDescription = Description.createSuiteDescription(cleanedName)
      engineDescription.addChild(ScenarioDescription)
      ScenarioMap = ScenarioMap + (ScenarioDescription -> s.asInstanceOf[Scenario])
      saveResults(clazz, engineDescription, engine)
    }
  }

  def fileFor(clazz: Class[Any], ed: Description, extension: String) = new File(AutoTddRunner.directory, clazz.getName() + "." + ed.getDisplayName() + "." + extension)

  def saveResults(clazz: Class[Any], ed: Description, e: Any) {
    import Files._
    AutoTddRunner.directory.mkdirs()
    printToFile(fileFor(clazz, ed, "attd"))((pw) => pw.write(e.toString))
    //        new File(AutoTddRunner.directory, clazz.getName() + "." + ed.getDisplayName() + ".attd"))((pw) => pw.write(e.toString))
  }

  def run(notifier: RunNotifier) {
    EngineTest.test(() => {
      notifier.fireTestStarted(getDescription)
      for (ed <- getDescription.getChildren) {
        notifier.fireTestStarted(ed)
        val engine = engineMap(ed)
        for (cd <- ed.getChildren) {
          notifier.fireTestStarted(cd)
          val Scenario = ScenarioMap(cd)
          if (EngineTest.exceptions.contains(Scenario))
            notifier.fireTestFailure(new Failure(cd, EngineTest.exceptions(Scenario)))
          else {
            //            val b = engine.makeClosureForBecause(Scenario.params);
            if (engine.root == null)
              notifier.fireTestIgnored(cd)
            else
              try {
                val actual = engine.applyParam(engine.root, Scenario.params, true)
                Assert.assertEquals(Scenario.expected, Some(actual))
                notifier.fireTestFinished(cd)
              } catch {
                //              case e: AssertionFailedError => notifier.fireTestFailure(new Failure(cd, e))
                case e: Throwable => notifier.fireTestFailure(new Failure(cd, e))
              }
          }
          notifier.fireTestFinished(ed)
        }
        //        println("Scenarios for: " + ed.getDisplayName())
        //        for (c <- engine.Scenarios)
        //          println("  " + c)
      }
      notifier.fireTestFinished(getDescription)
    })
  }

  def returnTypeIsEngine(m: Method): Boolean = {
    val rt = m.getReturnType()
    return isEngine(rt)
  }

  def typeIsEngine(f: Field): Boolean = {
    val rt = f.getType()
    return isEngine(rt)
  }

  def isEngine(rt: Class[_]): Boolean = {
    val c = classOf[Engine]
    if (c.isAssignableFrom(rt))
      return true;
    for (t <- rt.getInterfaces())
      if (c.isAssignableFrom(t))
        return true;
    return false;
  }

  def instantiate(clazz: Class[_]): Any = {
    val rm = ru.runtimeMirror(clazz.getClassLoader())
    val declaredFields = clazz.getDeclaredFields().toList
    val moduleField = declaredFields.find(field => field.getName() == "MODULE$")
    try {
      val obj = moduleField match {
        case Some(modField) => modField.get(clazz)
        case None => clazz.newInstance()
      }
      obj
    } catch {
      case e: Throwable =>
        throw new RuntimeException(s"Class: $clazz Field: $moduleField", e);
    }
  }

  trait SomeTrait { def someMethod: String }
  object SomeObject extends SomeTrait { def someMethod = "something" }

  class SomeClass extends SomeTrait { def someMethod = "something" }

  object Main {
    def main(args: Array[String]) = {
      val someClassTrait: SomeTrait = Class.forName("SomeClass").newInstance().asInstanceOf[SomeTrait]
      println("calling someClassTrait: " + someClassTrait.someMethod)
      val objectName = "SomeObject$"
      val cons = Class.forName(objectName).getDeclaredConstructors();
      cons(0).setAccessible(true);
      val someObjectTrait: SomeTrait = cons(0).newInstance().asInstanceOf[SomeTrait]
      println("calling someObjectTrait: " + someObjectTrait.someMethod)
    }
  }
}