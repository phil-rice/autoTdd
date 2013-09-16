package org.autotdd.engine.test

import org.autotdd.engine.AbstractEngine1Test
import org.autotdd.engine.DisplayTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.autotdd.engine._
import org.autotdd.engine.tests._
import org.autotdd.engine.tests.AutoTddJuitRunner
import org.junit.runner._
import org.junit.runner.notification._

class AutoTddRunnerForTests extends AutoTddRunner {
  val getDescription = Description.createSuiteDescription("Test")
}

class RunListenerForTests extends RunListener {
  var list = List[String]()

  def log(name: String, param: Any) = list = (name + ": " + param) :: list

  override def testRunStarted(description: Description) = log("testRunStarted", description)
  override def testRunFinished(result: Result) = log("testRunFinished", result)
  override def testStarted(description: Description) = log("testStarted", description)
  override def testFinished(description: Description) = log("testFinished", description)
  override def testFailure(failure: Failure) = log("testFailure", failure)
  override def testAssumptionFailure(failure: Failure) = log("testAssumptionFailure", failure)
  override def testIgnored(description: Description) = log("testIgnored", description)
}
@RunWith(classOf[JUnitRunner])
class AutoTddRunnerTests extends AbstractEngine1Test[String, String] {

  "An engine" should "act as a JUnit test" in {
    val engine1 = builder.useCase("uc1").
      scenario("one", "d1").
      expected("exp").build
    assertEquals(Map(), engine1.scenarioExceptionMap.map)
    assertEquals(List(
      "testStarted: Test",
      "testStarted: Engine",
      "testStarted: uc1",
      "testStarted: d1 => ep ", //the space is there to separate the because
      "testFinished: d1 => ep ",
      "testFinished: uc1",
      "testFinished: Engine",
      "testFinished: Test"), runAndGetListOfNotifications(engine1))
  }

  def runAndGetListOfNotifications(engine: Engine) = {
    val runner = new AutoTddRunnerForTests
    runner.addEngineForTest("Engine", engine)
    val listener = new RunListenerForTests
    val notifier = new RunNotifier()
    notifier.addFirstListener(listener)
    runner.run(notifier)
    listener.list.reverse
  }

}