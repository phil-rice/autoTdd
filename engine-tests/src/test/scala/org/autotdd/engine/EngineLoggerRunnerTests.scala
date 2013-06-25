package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EngineLoggerRunnerTests extends EngineStringStringTests {

  "An empty engine" should "log the default solution" in {
    val engine = Engine1("Z", UseCase[B, RFn, String]("")).withLogger(new TestLogger());
    engine("a")
    engine("b")
    checkMessages(engine, 
        "DEBUG Run()  Executing a", 
        "DEBUG Run()   Result Z", 
        "DEBUG Run()  Executing b", 
        "DEBUG Run()   Result Z")
  }

  it should "log executions with simple if then" in {
    val scenario = Scenario("A").becauseBecause("A").produces(("X"))
    val engine = Engine1("Z", UseCase("", scenario)).withLogger(new TestLogger());
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