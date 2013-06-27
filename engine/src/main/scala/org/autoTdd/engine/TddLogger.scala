package org.autotdd.engine

import org.apache.log4j.Logger
import org.apache.log4j.Level

trait LoggerDisplay {
  def loggerDisplay: String
}

case class ClassFunction[C, T](clazz: Class[C], fn: (C) => T) {
  def apply(c: C) = fn(c)
}

case class ClassFunctionList[T](list: List[ClassFunction[_, T]] = List()) {
  def apply[C](c: C) =
    list.collectFirst({ case ClassFunction(clazz: Class[C], f) => f(c) })
  def getOrElse[C](c: C, default: => T): T =
    apply(c).getOrElse(default)

}

trait LoggerDisplayProcessor {
  def displayMap: ClassFunctionList[String]
  def loggerDisplay(a: Any): String =
    displayMap.getOrElse(a,
      a match {
        case d: LoggerDisplay => d.loggerDisplay;
        case a => a.toString
      })
}

object TddLogger {
  val logger = Logger.getLogger(classOf[TddLogger]);
  val noLogger = new NoLogger()
  def loggerPriority = logger.getPriority()
  def log(priority: Level, msg: String) = logger.log(priority, msg);
  sealed abstract class TddMessageType(val name: String)
  case class Compile extends TddMessageType("Compile")
  case class Run extends TddMessageType("Run")
  val compile = Compile();
  val run = Run();

}

trait TddLogger extends LoggerDisplayProcessor {
  import TddLogger._
  /** The raw log message that writes the string to an output */
  protected def message(priority: Level, msgType: TddMessageType, message: => String)

  def infoRun(msg: => String) = message(Level.INFO, run, msg)
  def debugRun(msg: => String) = message(Level.DEBUG, run, msg)
  def warnRun(msg: => String) = message(Level.WARN, run, msg)
  def errorRun(msg: => String) = message(Level.ERROR, run, msg)
  def fatalRun(msg: => String) = message(Level.FATAL, run, msg)

  def infoCompile(msg: => String) = message(Level.INFO, compile, msg)
  def debugCompile(msg: => String) = message(Level.DEBUG, compile, msg)
  def warnCompile(msg: => String) = message(Level.WARN, compile, msg)
  def errorCompile(msg: => String) = message(Level.ERROR, compile, msg)
  def fatalCompile(msg: => String) = message(Level.FATAL, compile, msg)
}

class Log4JLogger(override val displayMap: ClassFunctionList[String] = ClassFunctionList()) extends TddLogger {
  import TddLogger._

  protected def message(priority: Level, msgType: TddMessageType, message: => String) {
    if (priority.isGreaterOrEqual(loggerPriority))
      logger.log(priority, message)
  }

}

class TestLogger(override val displayMap: ClassFunctionList[String] = ClassFunctionList()) extends TddLogger {
  import TddLogger._
  private var list = List[String]()
  protected def message(priority: Level, msgType: TddMessageType, message: => String) {
    list = String.format("%-5s %-6s %s", priority, msgType, message) :: list
  }

  def messages = list.reverse

}
class NoLogger extends TddLogger {
  import TddLogger._
  val displayMap: ClassFunctionList[String] = ClassFunctionList()
  protected def message(priority: Level, msgType: TddMessageType, message: => String) {}

}