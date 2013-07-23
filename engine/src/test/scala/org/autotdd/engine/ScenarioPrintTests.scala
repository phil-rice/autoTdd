package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ScenarioPrintTests extends EngineStringStringTests with JunitUniverse[String] {
  val manipulator = new JUnitTestManipulator()
  val bldr = builder.useCase("uc1").
    scenario("s1_1").expected("a").scenario("s1_2").expected("b").
    useCase("uc2").scenario("s2_1").expected("c").scenario("s2_2").expected("d")

  "Walking a scenario" should "visit each scenario and use case " in {

    var actualUseCaseStrings = List[String]();
    var actualEndUseCaseStrings = List[String]();
    var actualScenarioStrings = List[String]();
    var started = false
    bldr.walkScenarios(new ScenarioVisitor() {
      def start = if (actualUseCaseStrings ::: actualEndUseCaseStrings ::: actualScenarioStrings != List()) throw new IllegalStateException; started = true
      def visitUseCase(ui: Int, u: UseCase) = actualUseCaseStrings = (ui + "/" + u.description) :: actualUseCaseStrings
      def visitUseCaseEnd(u: UseCase) = actualEndUseCaseStrings = u.description :: actualEndUseCaseStrings
      def visitScenario(ui: Int, u: UseCase, si: Int, s: Scenario) = actualScenarioStrings = (ui + "/" + u.description + "/" + si + "/" + s.expected.get) :: actualScenarioStrings
    }, false)

    assert(started)
    assertEquals(List("0/uc2", "1/uc1"), actualUseCaseStrings.reverse)
    assertEquals(List("uc2", "uc1"), actualEndUseCaseStrings.reverse)
    assertEquals(List("0/uc2/0/d", "0/uc2/1/c", "1/uc1/0/b", "1/uc1/1/a"), actualScenarioStrings.reverse)
  }
  "Walking a scenario in reverse" should "visit each scenario and use case " in {

    var actualUseCaseStrings = List[String]();
    var actualEndUseCaseStrings = List[String]();
    var actualScenarioStrings = List[String]();
    var started = false
    bldr.walkScenarios(new ScenarioVisitor() {
      def start = if (actualUseCaseStrings ::: actualEndUseCaseStrings ::: actualScenarioStrings != List()) throw new IllegalStateException; started = true
      def visitUseCase(ui: Int, u: UseCase) = actualUseCaseStrings = (ui + "/" + u.description) :: actualUseCaseStrings
      def visitUseCaseEnd(u: UseCase) = actualEndUseCaseStrings = u.description :: actualEndUseCaseStrings
      def visitScenario(ui: Int, u: UseCase, si: Int, s: Scenario) = actualScenarioStrings = (ui + "/" + u.description + "/" + si + "/" + s.expected.get) :: actualScenarioStrings
    }, true)

    assert(started)
    assertEquals(List("0/uc1", "1/uc2"), actualUseCaseStrings.reverse)
    assertEquals(List("uc1", "uc2"), actualEndUseCaseStrings.reverse)
    assertEquals(List("0/uc1/0/a", "0/uc1/1/b", "1/uc2/0/c", "1/uc2/1/d"), actualScenarioStrings.reverse)
  }

  "The Junit scebario printer" should "produce an HTML entry for each scenario and use case with default values if not specified" in {

    val visitor = new JunitScenarioReporter(manipulator, logger)
    bldr.walkScenarios(visitor, true);
    val results = manipulator.results.reverse
    assert(results.contains("<h1>Usecase 0: uc1</h1>"), results);
    assert(results.contains("<h1>Usecase 1: uc2</h1>"), results);
    val scenariosText = results.filter(_.contains("<table>"))
    checkContents(scenariosText(0), "Parameters:s1_1", "Expected:a");
    checkContents(scenariosText(1), "Parameters:s1_2", "Expected:b");
    checkContents(scenariosText(2), "Parameters:s2_1", "Expected:c");
    checkContents(scenariosText(3), "Parameters:s2_2", "Expected:d");

    checkContentsNotThere(scenariosText(0), "Because", "Code");
    checkContentsNotThere(scenariosText(1), "Because", "Code");
    checkContentsNotThere(scenariosText(2), "Because", "Code");
    checkContentsNotThere(scenariosText(3), "Because", "Code");
  }

  it should "produce an HTML entry with because if specified" in {
    val bldr = builder.useCase("uc1").scenario("s1_1").expected("a").because((x: String) => true, "comment")
    val visitor = new JunitScenarioReporter(manipulator, logger)
    bldr.walkScenarios(visitor, true);
    val results = manipulator.results.reverse
    val scenariosText = results.filter(_.contains("<table>"))
    checkContents(scenariosText(0), "Parameters:s1_1", "Expected:a", "Because:");
  }
  it should "produce an HTML entry with code if specified" in {
    fail
  }
  it should "produce an HTML entry with comments in third column if specified" in {
    fail
  }

  def checkContentsNotThere(scenariosText: String, notExpectedList: String*) {
    for (s <- notExpectedList) {
      val notExpected = <td>{ s }</td>.mkString
      assert(!scenariosText.contains(notExpected), s"NotExpected is $s\n scenariosText is $scenariosText")
    }
  }

  def checkContents(scenariosText: String, expectedList: String*) {
    val Splitter = "(\\w+):([\\w<>]+)".r
    for (s <- expectedList) {
      val Splitter(a, b) = s
      val expected = <tr><td>{ a }</td><td>{ b }</td></tr>.mkString
      assert(scenariosText.contains(expected), s"Expected is $expected\n scenariosText is $scenariosText")
    }
  }

}