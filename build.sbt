name := "vedaa-web"

version := "1.0"

scalaVersion := "2.9.1"

libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided"


unmanagedJars in Compile += Attributed.blank(file("E:/prog/jvm/lib/vedaa-template/target/scala-2.9.1/vedaa-template_2.9.1-1.0.jar"))
