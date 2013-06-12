import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.autotdd",
    version := "1.0.0",
    scalacOptions ++= Seq(),
    scalaVersion := "2.10.1",

    libraryDependencies ++= Seq(
		"org.scala-lang" % "scala-reflect" % "2.10.1",
		"org.scala-lang" % "scala-compiler" % "2.10.1",
		"org.scalatest" % "scalatest_2.10" % "1.9.1",
		"junit" % "junit" % "4.8.2"
	))  
}

object HelloBuild extends Build {
    import BuildSettings._
    lazy val constraint = Project(id = "constraint",settings=buildSettings,base = file("constraint")) 
    lazy val engine = Project(id = "engine",settings=buildSettings,base = file("engine")) aggregate(constraint) dependsOn(constraint)
    lazy val engine_test = Project(id = "engine_test",settings=buildSettings,base = file("engine-tests")) aggregate(constraint,engine) dependsOn(constraint,engine)
    lazy val examples = Project(id = "examples",settings=buildSettings,base = file("examples")) aggregate(constraint,engine) dependsOn(constraint,engine)
    lazy val root = Project(id = "root",settings=buildSettings,base = file(".")) aggregate(constraint,engine,examples,engine_test)
}