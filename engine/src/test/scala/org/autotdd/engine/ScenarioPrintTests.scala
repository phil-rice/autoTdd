package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ScenarioPrintTests extends EngineStringStringTests with JunitUniverse[String] {

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
      def visitUseCase(u: UseCase) = actualUseCaseStrings = u.description :: actualUseCaseStrings
      def visitUseCaseEnd(u: UseCase) = actualEndUseCaseStrings = u.description :: actualEndUseCaseStrings
      def visitScenario(u: UseCase, index: Int, s: Scenario) = actualScenarioStrings = (u.description + "/" + index + "/" + s.expected.get) :: actualScenarioStrings
    })

    assert(started)
    assertEquals(List("uc1", "uc2"), actualUseCaseStrings.reverse)
    assertEquals(List("uc1", "uc2"), actualEndUseCaseStrings.reverse)
    assertEquals(List("uc1/0/a", "uc1/1/b", "uc2/0/c", "uc2/1/d"), actualScenarioStrings.reverse)
  }

  "The Junit scebario printer" should "produce an HTML entry for each scenario and use case with default values if not specified" in {
    val manipulator = new JUnitTestManipulator()
    val visitor=new JunitScenarioReporter(manipulator, logger)
    bldr.walkScenarios(visitor);
    val results = manipulator.results
    assert(results.contains("<h1>uc1</h1>"), results);
    assert(results.contains("<h1>uc2</h1>"), results);
    val scenariosText = results.reverse.filter(_.contains("<table>"))
    checkContents(scenariosText(0), "Parameters:s1_1", "Expected:a", "Because:<undefined>", "Code:a");
    checkContents(scenariosText(1), "Parameters:s1_2", "Expected:b", "Because:<undefined>", "Code:b");
    checkContents(scenariosText(2), "Parameters:s2_1", "Expected:c", "Because:<undefined>", "Code:c");
    checkContents(scenariosText(3), "Parameters:s2_2", "Expected:d", "Because:<undefined>", "Code:d");
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