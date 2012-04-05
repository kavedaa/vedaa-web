name := "vedaa-web"

version := "1.0"

scalaVersion := "2.9.1"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided"


unmanagedJars in Compile += Attributed.blank(file("E:/prog/jvm/lib/vedaa-template-lib/dist/vedaa-template-lib.jar"))
