package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.autotdd.engine._

case class DisplayTest(val x: String) extends LoggerDisplay {
  def loggerDisplay(dp: LoggerDisplayProcessor) = "{" + x + "}"
}


@RunWith(classOf[JUnitRunner])
class EngineLoggerRunnerDisplayTests extends EngineTests[DisplayTest] with Engine1Types[DisplayTest, DisplayTest] {
  implicit def string_to_because(s: String) = new Because[B]((x) => x.x contains s, s.toString())
  implicit def string_to_rfn2(s: String): (DisplayTest) => DisplayTest = (x: DisplayTest) => DisplayTest(s)
  implicit def string_to_display_test(s: String) = DisplayTest(s)
  val emptyUsecase = UseCase[B, RFn, DisplayTest]("")

  "A logger display processor" should "use the display method if available" in {
    val processor = LoggerDisplayProcessor()
    assert("msg" == processor("msg"))
    assert("1" == processor(1))
    assert("{1}" == processor(DisplayTest("1")))
    assert("msg" == processor(new LoggerDisplay() { def loggerDisplay (dp: LoggerDisplayProcessor)= "msg" }))
  }

  it should "use the class function list in preference to the display method " in {
    val processor = new SimpleLoggerDisplayProcessor(ClassFunctionList(List(ClassFunction(classOf[DisplayTest], (d: DisplayTest) => "<" + d.x + ">"))))
    assert("msg" == processor("msg"))
    assert("1" == processor(1))
    assert("<1>" == processor(DisplayTest("1")))
    assert("<1>" == processor(new DisplayTest("1"){}))
  }

  "An empty engine" should "log the default solution" in {
    val engine: Engine1[DisplayTest, DisplayTest] = Engine1[DisplayTest, DisplayTest]("Z": RFn, emptyUsecase).withLogger(new TestLogger());
    engine("a")
    engine("b")
    checkMessages(engine,
      "DEBUG Run()  Executing {a}",
      "DEBUG Run()   Result {Z}",
      "DEBUG Run()  Executing {b}",
      "DEBUG Run()   Result {Z}")
  }

  it should "log executions with simple if then" in {
    val emptyUsecase = UseCase[B, RFn, DisplayTest]("")
    val scenario = Scenario[DisplayTest, DisplayTest]("A").becauseBecause(string_to_because("A")).produces(DisplayTest("X"))
    val engine = Engine1[DisplayTest, DisplayTest]("Z": RFn, UseCase("", scenario)).withLogger(new TestLogger());
    engine("A")
    engine("B")
    checkLastMessages(engine,
      "DEBUG Run()  Executing {A}",
      "INFO  Run()   ConditionBecause(A) was true",
      "DEBUG Run()   Result {X}",
      "DEBUG Run()  Executing {B}",
      "INFO  Run()   ConditionBecause(A) was false",
      "DEBUG Run()   Result {Z}")
  }

}
@RunWith(classOf[JUnitRunner])
class EngineLoggerRunnerStringTests extends EngineStringStringTests {
  val emptyUsecase = UseCase[B, RFn, String]("")

  "A logger display processor" should "use the display method if available" in {
    val processor = new SimpleLoggerDisplayProcessor() {}
    assert("msg" == processor("msg"))
    assert("1" == processor(1))
    assert("msg" == processor(new LoggerDisplay() { def loggerDisplay(dp: LoggerDisplayProcessor) = "msg" }))
  }

  "An empty engine" should "log the default solution" in {
    val engine: Engine1[String, String] = Engine1[String, String]("Z": RFn, emptyUsecase).withLogger(new TestLogger());
    engine("a")
    engine("b")
    checkMessages(engine,
      "DEBUG Run()  Executing a",
      "DEBUG Run()   Result Z",
      "DEBUG Run()  Executing b",
      "DEBUG Run()   Result Z")
  }

  it should "log executions with simple if then" in {
    val emptyUsecase = UseCase[B, RFn, String]("")
    val scenario = Scenario[String, String]("A").becauseBecause("A").produces("X")
    val engine = Engine1[String, String]("Z": RFn, UseCase("", scenario)).withLogger(new TestLogger());
    assert("X" == engine("A"))
    assert("Z" == engine("B"))
    checkLastMessages(engine,
      "DEBUG Run()  Executing A",
      "INFO  Run()   ConditionBecause(A) was true",
      "DEBUG Run()   Result X",
      "DEBUG Run()  Executing B",
      "INFO  Run()   ConditionBecause(A) was false",
      "DEBUG Run()   Result Z")
  }

}