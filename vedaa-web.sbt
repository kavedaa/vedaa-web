name := "vedaa-web"

version := "1.5-SNAPSHOT"

organization := "no.vedaadata"

scalaVersion := "2.13.4"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided"

libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.3.0"

libraryDependencies += "commons-fileupload" % "commons-fileupload" % "1.3.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.5" % "test"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature"
)

