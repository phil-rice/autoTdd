package org.autoTdd.engine.tests

import org.junit.runner.Description
import scala.collection.JavaConversions._
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier
import org.autoTdd.engine._
import scala.reflect.runtime.{ universe => ru }
import java.lang.reflect.Method
import junit.framework.Assert
import org.junit.runner.notification.Failure
import org.autotdd.constraints.Constraint

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

class AutoTddRunner(val clazz: Class[Any]) extends Runner {

  val getDescription = Description.createSuiteDescription("ATDD: " + clazz.getName);

  val instance = EngineTest.test(() => { instantiate(clazz) });

  var engineMap: Map[Description, Engine[Any]] = Map()
  var constraintMap: Map[Description, Constraint[Any, Any, Any, Any]] = Map()
  var exceptionMap: Map[Description, Throwable] = Map()

  for (m <- clazz.getDeclaredMethods().filter((m) => returnTypeIsEngine(m))) {
    val engineDescription = Description.createSuiteDescription(m.getName())
    getDescription.addChild(engineDescription)
    val engine: Engine[Any] = m.invoke(instance).asInstanceOf[Engine[Any]];
    println(m.getName())
    println(engine)
    engineMap = engineMap + (engineDescription -> engine)
    for (c <- engine.constraints) {
      val name = c.params.reduce((acc, p) => acc + ", " + p) + " => " + c.expected + " "+ c.becauseString
      val cleanedName = name.replace("(", "<").replace(")", ">");
//      println("   " + cleanedName)
      val constraintDescription = Description.createSuiteDescription(cleanedName)
      engineDescription.addChild(constraintDescription)
      constraintMap = constraintMap + (constraintDescription -> c.asInstanceOf[Constraint[Any, Any, Any, Any]])
    }
  }

  def run(notifier: RunNotifier) {
    EngineTest.test(() => {
      notifier.fireTestStarted(getDescription)
      for (ed <- getDescription.getChildren) {
        notifier.fireTestStarted(ed)
        val engine = engineMap(ed)
        for (cd <- ed.getChildren) {
          notifier.fireTestStarted(cd)
          val constraint = constraintMap(cd)
          if (EngineTest.exceptions.contains(constraint))
            notifier.fireTestFailure(new Failure(cd, EngineTest.exceptions(constraint)))
          else {
            val b = engine.makeClosureForBecause(constraint.params);
            val actual = engine.applyParam(constraint.params)
            try {
              Assert.assertEquals(constraint.expected, actual)
              notifier.fireTestFinished(cd)
            } catch {
              case e: Throwable => notifier.fireTestFailure(new Failure(cd, e))
            }
          }
          notifier.fireTestFinished(ed)
        }
//        println("Constraints for: " + ed.getDisplayName())
//        for (c <- engine.constraints)
//          println("  " + c)
      }
      notifier.fireTestFinished(getDescription)
    })
  }
  def returnTypeIsEngine(m: Method): Boolean = {
    val rt = m.getReturnType()
    val c = classOf[MutableEngine[Any]]
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