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

object AutoTddRunner {
  val separator = "\n#########\n"
  val userHome = System.getProperty("user.home");
  val directory = new File(userHome, ".autoTdd")

}
class AutoTddRunner(val clazz: Class[Any]) extends Runner {

  val getDescription = Description.createSuiteDescription("ATDD: " + clazz.getName);

  val instance = EngineTest.test(() => { instantiate(clazz) });

  var engineMap: Map[Description, Engine[Any]] = Map()
  var ScenarioMap: Map[Description, Scenario[Any, Any, Any]] = Map()
  var exceptionMap: Map[Description, Throwable] = Map()

  for (m <- clazz.getDeclaredMethods().filter((m) => returnTypeIsEngine(m))) {
    val engineDescription = Description.createSuiteDescription(m.getName())
    getDescription.addChild(engineDescription)
    val engine: Engine[Any] = m.invoke(instance).asInstanceOf[Engine[Any]];
    println(m.getName())
    println(engine)
    engineMap = engineMap + (engineDescription -> engine)
    for (c <- engine.scenarios) {
      val name = c.params.reduce((acc, p) => acc + ", " + p) + " => " + c.expected + " " + c.becauseString
      val cleanedName = name.replace("(", "<").replace(")", ">");
      //      println("   " + cleanedName)
      val ScenarioDescription = Description.createSuiteDescription(cleanedName)
      engineDescription.addChild(ScenarioDescription)
      ScenarioMap = ScenarioMap + (ScenarioDescription -> c.asInstanceOf[Scenario[ Any, Any, Any]])
      saveResults(clazz, engineDescription, engine)
    }

  }

  def saveResults(clazz: Class[Any], ed: Description, e: Any) {
    AutoTddRunner.directory.mkdirs()
    printToFile(new File(AutoTddRunner.directory, clazz.getName() + "." + ed.getDisplayName() + ".attd"))((pw) => pw.write(e.toString))
  }

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
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
            val b = engine.makeClosureForBecause(Scenario.params);
            val actual = engine.applyParam(engine.root, Scenario.params, true)
            try {
              Assert.assertEquals(Scenario.expected, Some(actual))
              notifier.fireTestFinished(cd)
            } catch {
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
    val c = classOf[Engine[Any]]
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
    val obj = declaredFields.find(field => field.getName() == "MODULE$") match {
      case Some(modField) => modField.get(clazz)
      case None => clazz.newInstance()
    }
    obj
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