import sbt._
import Keys._

object AutoTddBuild extends Build {
	lazy val root = Project(id = "autoTdd", base = file(".")) aggregate (constraint,engine,engine_tests)

	lazy val constraint = Project(id = "constraint", base = file("constraint")) 

  lazy val engine = Project(id = "engine", base = file("engine")) aggregate (constraint) dependsOn(constraint)

  lazy val engine_tests = Project(id = "engine-tests", base = file("engine-tests")) aggregate (constraint,engine) dependsOn(constraint, engine)
}