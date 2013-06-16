import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys

object BuildSettings {



  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.autotdd",
    version := "1.0.0",
    scalacOptions ++= Seq(),
	retrieveManaged := true,
    scalaVersion := "2.10.1",
    EclipseKeys.withSource := true,
    EclipseKeys.eclipseOutput := Some("bin"),
    libraryDependencies ++= Seq(
		"org.scala-lang" % "scala-reflect" % "2.10.1",
		"org.scala-lang" % "scala-compiler" % "2.10.1",
		"org.scalatest" % "scalatest_2.10" % "1.9.1",
		
		"junit" % "junit" % "4.8.2"
	))  

   val eclipseSettings = buildSettings ++ Seq(
    resolvers += "eclipse-repo" at "https://swt-repo.googlecode.com/svn/repo/",
    libraryDependencies ++= Seq(
	    "com.miglayout" % "miglayout-swt" % "4.2",
		"org.autotdd" %% "constraint" % "1.0.0",
        "org.autotdd" %% "engine" % "1.0.0",
		"org.eclipse.equinox" % "org.eclipse.equinox.common" % "3.6.0.v20100503",
		"org.eclipse.ui" % "org.eclipse.ui.workbench" % "3.7.1.v20120104-1859",
		"org.eclipse.swt.win32.win32" % "x86" % "3.3.0-v3346",
		"org.eclipse.core" % "org.eclipse.core.runtime" % "3.6.0.v20100505"
   ))
	
}

object HelloBuild extends Build {
    import BuildSettings._
	
	val copy = TaskKey[Unit]("copy", "Copies files to eclipse project")

    val copyTask = copy := {
      copy("lib_managed", "eclipse2/libFromSbt")
    }
	
	
    lazy val constraint = Project(id = "constraint",settings=buildSettings,base = file("constraint")) 
    lazy val engine = Project(id = "engine",settings=buildSettings,base = file("engine")) dependsOn(constraint)
    lazy val engine_test = Project(id = "engine_test",settings=buildSettings,base = file("engine-tests")) dependsOn(constraint,engine)
    lazy val examples = Project(id = "examples",settings=buildSettings,base = file("examples")) dependsOn(constraint,engine)
    lazy val eclipse = Project(id = "eclipse",settings=eclipseSettings,base = file("eclipse2")) 
    lazy val root = Project(id = "root",settings=buildSettings ++ Seq(copyTask),base = file(".")) aggregate(constraint,engine,examples,engine_test,eclipse)
	
	  import java.io.File

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    if (these == null)
      Array();
    else
      these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  def copy(start: String, to: String) {
    val s = new File(start)
    val t = new File(to)
    for (
      f <- recursiveListFiles(s) //
      if f.getName().endsWith(".jar") //
      if (!f.getPath().contains("org.eclipse")) //
      if (!f.getPath().contains("org.osgi")) //
      if (!f.getPath().contains("servlet-api"))
    ) {
      val d = new File(t, f.getName())
      println("Copying " + f + " to " + d);
      copy(f, d)
    }
  }

  def copy(sourceFile: File, destFile: File) {
    import java.nio.channels.FileChannel
    import java.io.FileInputStream
    import java.io.FileOutputStream

    if (!destFile.exists())
      destFile.createNewFile();

    val source = new FileInputStream(sourceFile).getChannel();
    val destination = new FileOutputStream(destFile).getChannel();
    destination.transferFrom(source, 0, source.size());
  }
	
	
}
