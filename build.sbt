
lazy val commonSettings = Seq(
  organization := "io.spinor",
  version := "1.0.0-SNAPSHOT",
  scalaVersion := "2.11.8"
)

lazy val igApi = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "ig-api",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.1",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.8.4",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.4",
      "com.fasterxml.jackson.core" % "jackson-annotations" % "2.8.4",
      "org.apache.httpcomponents" % "httpclient" % "4.5.2",
      "org.apache.commons" % "commons-lang3" % "3.5",
      "org.scalatest" %% "scalatest" % "2.2.6" % "test"
    ),
    unmanagedJars in Compile ++=
     (file("lib/") * "ls-client.jar").classpath
  )

fork in run := true
