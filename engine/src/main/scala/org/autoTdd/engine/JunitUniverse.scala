package org.autotdd.engine

import java.io.File

trait JunitUniverse[R] extends EngineUniverse[R] {

  trait JUnitManipulator {

    def nuke;
    def append(s: String);
  }

  class JunitFileManipulator(val file: File) extends JUnitManipulator {
    def nuke =
      file.delete()
    def append(s: String) =
      Files.appendToFile(file)((pw) => pw.append(s))
  }

  class JUnitTestManipulator() extends JUnitManipulator {
    var results: List[String] = null
    def nuke =
      if (results == null)
        results = List()
      else
        throw new IllegalStateException;
    def append(s: String) = results = s :: results
  }

  class JunitScenarioReporter(manipulator: JUnitManipulator, displayProcessor: LoggerDisplayProcessor) extends ScenarioVisitor {

    def start = manipulator.nuke

    def visitUseCase(ui: Int, u: UseCase) {
      val text = <h1>Usecase { ui }: { u.description }</h1>.mkString;
      manipulator.append(text)
    }

    def visitUseCaseEnd(u: UseCase) {
    }

    def visitScenario(useCaseindex: Int, u: UseCase, scenarioIndex: Int, s: Scenario) {
      val paramsString = s.params.mkString("<br />,")
      val text =
        <h2>{ scenarioIndex }{ s.description.collect { case d => ": " + d } getOrElse ("") }</h2>
        <table>
          <tr><td>Parameters</td><td><pre>{ s.params.mkString(",") }</pre></td></tr>
          <tr><td>Expected</td><td>{ s.expected.getOrElse("<undefined>") }</td></tr>
          {
            if (s.because.isDefined)
              <tr><td>Because </td><td> { s.becauseString } </td><td>{ s.because.get.comment }</td></tr>
          }
          {
            if (s.code.isDefined)
              <tr><td>Code</td><td>{ s.code.get.description }</td><td>{ s.code.get.comment }</td></tr>
          }
          {
            if (s.assertions.size > 0)
              <tr><td>Assertions</td><td>{ s.assertions.mkString(",") }</td></tr>
          }
        </table>;

      val t = text.mkString
      manipulator.append(t)
    }

    def end={}
    
    def ifPrint: (String, Node) => String = ???
    def elsePrint: (String, Node) => String = ???
    def endPrint: (String, Node) => String = ???
  }

}  