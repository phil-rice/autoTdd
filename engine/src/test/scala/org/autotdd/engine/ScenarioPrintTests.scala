package org.autotdd.engine

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ScenarioPrintTests extends EngineStringStringTests with JunitUniverse[String] {

  val bldr = builder.withDescription("EngineDescription").useCase("uc1").
    scenario("s1_1").expected("a").scenario("s1_2").expected("b").
    useCase("uc2").scenario("s2_1").expected("c").scenario("s2_2").expected("d")

  "Walking a scenario" should "visit each scenario and use case " in {
    var actualUseCaseStrings = List[String]();
    var actualEndUseCaseStrings = List[String]();
    var actualScenarioStrings = List[String]();
    var actualDescription: Option[String] = None
    var started = false
    var ended = false;
    bldr.walkScenarios(new ScenarioVisitor() {
      def start(engineDescription: Option[String]) = actualDescription = engineDescription; if (actualUseCaseStrings ::: actualEndUseCaseStrings ::: actualScenarioStrings != List()) throw new IllegalStateException; started = true; if (ended) throw new IllegalStateException
      def visitUseCase(ui: Int, u: UseCase) = actualUseCaseStrings = (ui + "/" + u.description) :: actualUseCaseStrings; if (ended) throw new IllegalStateException
      def visitUseCaseEnd(u: UseCase) = actualEndUseCaseStrings = u.description :: actualEndUseCaseStrings; if (ended) throw new IllegalStateException
      def visitScenario(ui: Int, u: UseCase, si: Int, s: Scenario) = actualScenarioStrings = (ui + "/" + u.description + "/" + si + "/" + s.expected) :: actualScenarioStrings; if (ended) throw new IllegalStateException
      def end = ended = true
    }, false)

    assert(started)
    assert(ended)
    assertEquals(List("0/uc2", "1/uc1"), actualUseCaseStrings.reverse)
    assertEquals(List("uc2", "uc1"), actualEndUseCaseStrings.reverse)
    assertEquals(List("0/uc2/0/d", "0/uc2/1/c", "1/uc1/0/b", "1/uc1/1/a"), actualScenarioStrings.reverse)
    assertEquals(Some("EngineDescription"), actualDescription)
  }

  "Walking a scenario in reverse" should "visit each scenario and use case " in {
    var actualUseCaseStrings = List[String]();
    var actualEndUseCaseStrings = List[String]();
    var actualScenarioStrings = List[String]();
    var started = false
    var ended = false;
    var actualDescription: Option[String] = None
    bldr.walkScenarios(new ScenarioVisitor() {
      def start(engineDescription: Option[String]) = actualDescription = engineDescription; if (actualUseCaseStrings ::: actualEndUseCaseStrings ::: actualScenarioStrings != List()) throw new IllegalStateException; started = true; if (ended) throw new IllegalStateException
      def visitUseCase(ui: Int, u: UseCase) = actualUseCaseStrings = (ui + "/" + u.description) :: actualUseCaseStrings; if (ended) throw new IllegalStateException
      def visitUseCaseEnd(u: UseCase) = actualEndUseCaseStrings = u.description :: actualEndUseCaseStrings; if (ended) throw new IllegalStateException
      def visitScenario(ui: Int, u: UseCase, si: Int, s: Scenario) = actualScenarioStrings = (ui + "/" + u.description + "/" + si + "/" + s.expected) :: actualScenarioStrings; if (ended) throw new IllegalStateException
      def end = ended = true
    }, true)

    assert(started)
    assert(ended)
    assertEquals(List("0/uc1", "1/uc2"), actualUseCaseStrings.reverse)
    assertEquals(List("uc1", "uc2"), actualEndUseCaseStrings.reverse)
    assertEquals(List("0/uc1/0/a", "0/uc1/1/b", "1/uc2/0/c", "1/uc2/1/d"), actualScenarioStrings.reverse)
    assertEquals(Some("EngineDescription"), actualDescription)
  }

  "The Junit scebario printer" should "produce an HTML entry for each scenario and use case with default values if not specified" in {
    val manipulator = new JUnitTestManipulator()
    val visitor = new JunitScenarioReporter(manipulator, logger)
    bldr.walkScenarios(visitor, true);
    val results = manipulator.results.reverse
    assert(results.contains("<h1>EngineDescription</h1>"), results);
    assert(results.contains("<h2>Usecase 0: uc1</h2>"), results);
    assert(results.contains("<h2>Usecase 1: uc2</h2>"), results);
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
    val manipulator = new JUnitTestManipulator()
    val visitor = new JunitScenarioReporter(manipulator, logger)
    bldr.walkScenarios(visitor, true);
    val results = manipulator.results.reverse
    val scenariosText = results.filter(_.contains("<table>"))
    checkContents(scenariosText(0), "Parameters:s1_1", "Expected:a", "Because:");
  }

  it should "Produce a h1 with the engine description in it" in {
    val bldr = builder.withDescription("descr").useCase("uc1").scenario("s1_1").expected("a").because((x: String) => true, "comment")
    val manipulator = new JUnitTestManipulator()
    val visitor = new JunitScenarioReporter(manipulator, logger)
    bldr.walkScenarios(visitor, true);
    val results = manipulator.results.reverse
    assertEquals("<h1>descr</h1>", results(0))
  }
  it should "not produce a h1 with no engine description " in {
    val bldr = builder.useCase("uc1").scenario("s1_1").expected("a").because((x: String) => true, "comment")
    val manipulator = new JUnitTestManipulator()
    val visitor = new JunitScenarioReporter(manipulator, logger)
    bldr.walkScenarios(visitor, true);
    val results = manipulator.results.reverse
    assert(!results(0).contains("<h1>"))
  }
  //TODO more HTML  it should "produce an HTML entry with code if specified" in {
  //    fail
  //  }
  //  it should "produce an HTML entry with comments in third column if specified" in {
  //    fail
  //  }

  def checkContentsNotThere(scenariosText: String, notExpectedList: String*) {
    for (s <- notExpectedList) {
      val notExpected = <td>{ s }</td>.mkString
      assert(!scenariosText.contains(notExpected), s"NotExpected is $s\n scenariosText is $scenariosText")
    }
  }

  private val findAColon = "^(\\w+):$".r
  private val findAColonB = "^(\\w+):([\\w<>]+)$".r
  def checkContents(scenariosText: String, expectedList: String*) {
    for (s <- expectedList) {
      val matchAColon: Option[String] = findAColon.findFirstIn(s)
      if (matchAColon.isDefined) {
        assert(!scenariosText.contains(matchAColon.get), s"Should not have is $s\n scenariosText is $scenariosText")

      } else {
        val findAColonB(a, b) = s
        val expected = <tr><td>{ a }</td><td><pre>{ b }</pre></td></tr>.mkString
        assert(scenariosText.contains(expected), s"Expected is $expected\n scenariosText is $scenariosText")
      }
    }
  }

}