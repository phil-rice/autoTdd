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
    def nuke = if (results == null) results = List() else throw new IllegalStateException;
    def append(s: String) = results = s :: results
  }

  class JunitScenarioReporter(manipulator: JUnitManipulator, displayProcessor: LoggerDisplayProcessor) extends ScenarioVisitor {

    def start = manipulator.nuke

    def visitUseCase(u: UseCase) {
      val text = <h1>{ u.description }</h1>.mkString;
      manipulator.append(text)
    }

    def visitUseCaseEnd(u: UseCase) {
      val text = s"Goodbye ${u.description}";
      manipulator.append(text)
    }

    def visitScenario(u: UseCase, index: Int, s: Scenario) {
      val paramsString = s.params.mkString("<br />,")
      val assertionsString = s.assertions.mkString(",")
      val becauseString = s.because.collect { case b => b.description }.getOrElse("<undefined>")
      val text =
        <h2>{ index } </h2>
        <table>
          <tr>
            <td>Parameters</td>
            <td>{ s.params.mkString(",") }</td>
          </tr>
          <tr><td>Expected</td><td>{ s.expected.getOrElse("<undefined>") }</td></tr>
          <tr><td>Because</td><td>{ becauseString }</td></tr>
          <tr><td>Code</td><td>{ s.actualCode.description }</td></tr>
          <tr><td>Assertions</td><td>{ assertionsString }</td></tr>
        </table>;

      val t = text.mkString
      manipulator.append(t)
    }

    def ifPrint: (String, Node) => String = ???
    def elsePrint: (String, Node) => String = ???
    def endPrint: (String, Node) => String = ???
  }

}  