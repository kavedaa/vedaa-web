name := "vedaa-web"

version := "1.5-SNAPSHOT"

organization := "no.vedaadata"

scalaVersion := "2.10.2"

crossScalaVersions := Seq("2.10.2", "2.11.5")

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// add scala-xml dependency when needed (for Scala 2.11 and newer) in a robust way
// this mechanism supports cross-version publishing
libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, add dependency on scala-xml module
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      Seq("org.scala-lang.modules" %% "scala-xml" % "1.0.2")
    case _ =>
      Nil
  }
}
