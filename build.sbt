
import AssemblyKeys._

assemblyOption in assembly ~= {
  _.copy(includeScala = false)
}

lazy val commonSettings = Seq(
  organization := "io.spinor",
  version := "1.0.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.1" % "provided",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0" % "provided",
    "ch.qos.logback" % "logback-classic" % "1.1.3" % "provided",
    "com.fasterxml.jackson.core" % "jackson-core" % "2.8.4" % "provided",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.4" % "provided",
    "com.fasterxml.jackson.core" % "jackson-annotations" % "2.8.4" % "provided",
    "org.apache.commons" % "commons-lang3" % "3.5" % "provided",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  )
)

lazy val igApi = (project in file(".")).
  settings(assemblySettings: _*).
  settings(commonSettings: _*).
  settings(
    artifact in(Compile, assembly) ~= { art =>
      art.copy(`classifier` = Some("assembly"))
    }
  ).
  settings(
    name := "ig-api",
    libraryDependencies ++= Seq(
      "org.apache.httpcomponents" % "httpclient" % "4.5.2" % "provided"
    ),
    unmanagedJars in Compile ++=
      (file("lib/") * "ls-client.jar").classpath
  ).
  settings(
      addArtifact(artifact in(Compile, assembly), assembly)
  )

publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository")))

fork in run := true
