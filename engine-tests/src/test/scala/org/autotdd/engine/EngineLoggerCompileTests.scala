package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class EngineLoggerCompileTests extends EngineStringStringTests {

  "An empty engine" should "log the change from root to if then with one scenario" in {
    val scenario = Scenario("A").produces("X").becauseBecause("A")
    val engine = Engine1("Z", UseCase("", scenario)).withLogger(new TestLogger());
    checkMessages(engine, "DEBUG Compile() Adding [0] as new root")
  }

  it should "Add scenario to root if adding assertion" in {
    val scenario = Scenario("A").becauseBecause("A").produces(("Z"))
    val engine = Engine1("Z", UseCase("", scenario)).withLogger(new TestLogger());
    checkMessages(engine, "DEBUG Compile() Adding [0] as extra scenario for root")
  }

  it should "log adding to no clause if because is false for root" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val b = Scenario("B").becauseBecause("B").produces("Y")
    val engine = Engine1[String, String]("Z", UseCase("", a, b)).withLogger(new TestLogger());
    checkMessages(engine, "DEBUG Compile() Adding [0] as new root", "DEBUG Compile() Adding [1] under no of node [0]")
  }

  it should "log adding to yes clause if because is true for root" in {
    val a = Scenario("A").becauseBecause("A").produces("X")
    val ab = Scenario("AB").becauseBecause("B").produces("Y")
    val engine = Engine1[String, String]("Z", UseCase("", a, ab)).withLogger(new TestLogger());
    checkMessages(engine, "DEBUG Compile() Adding [0] as new root", "DEBUG Compile() Adding [1] under yes of node [0]")
  }

  it should "log correctly when three scenarios are added" in {
    val becauseA = new Because[B]((h => h contains "A"), "hA");
    val becauseB = new Because[B]((h => h contains "B"), "hB");
    val becauseAB = new Because[B]((h => (h contains "A") & (h contains "B")), "hAB");
    val a = Scenario("A").produces("XA").becauseBecause(becauseA);
    val b = Scenario("B").produces("XB").becauseBecause(becauseB);
    val ab = Scenario("AB").produces("XAB").becauseBecause(becauseAB);

    val engine = Engine1[String, String]("Z", UseCase("", a, ab, b)).withLogger(new TestLogger)
    checkMessages(engine,
      "DEBUG Compile() Adding [0] as new root",
      "DEBUG Compile() Adding [1] under yes of node [0]",
      "DEBUG Compile() Adding [2] under no of node [0]")
  }
}