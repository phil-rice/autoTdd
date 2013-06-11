import sbt._
import Keys._

object AutoTddBuild extends Build {
  lazy val root = Project(id = "constraint", base = file("../constraint")) aggregate (foo, bar)

  lazy val foo = Project(id = "engine", base = file("../engine"))

  lazy val bar = Project(id = "engine-tests", base = file("../engine-tests"))
}