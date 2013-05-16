name := "hello"

version := "1.0"

scalaVersion := "2.10.1"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test" 

override def managedStyle = ManagedStyle.Maven

lazy val publishTo = Resolver.sftp("My Maven Repo", "maven.example.org", "/var/www/maven/html")