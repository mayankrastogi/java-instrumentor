name := "java-instrumentor"

version := "0.1"

scalaVersion in ThisBuild := "2.13.1"

scalacOptions in ThisBuild += "-target:jvm-1.11"


// Logback logging framework
lazy val logback = Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.gnieh" % "logback-config" % "0.4.0"
)

// Scalatest testing framework
lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.8" % "test"

// ---------------------------------------------------------------------------------------------------------------------
// Project definitions
// ---------------------------------------------------------------------------------------------------------------------

// The root project
lazy val root = (project in file("."))
  .aggregate(clientLib, tools)

// The client classes that will be inserted in the generated code of the input Java project for instrumentation
lazy val clientLib = (project in file("client-lib"))
  .settings(
    libraryDependencies ++= Seq(scalatest) ++ logback
  )

// The instrumentation tool that will read the input Java code, instrument it, and run the generated code
lazy val tools = (project in file("tools"))
  .settings(
    libraryDependencies ++= Seq(
      // Typesafe Configuration Library
      "com.typesafe" % "config" % "1.3.4",

      // Eclipse JDT for building AST
      "org.eclipse.platform" % "org.eclipse.equinox.app" % "1.4.300", // v1.3.0 is not available; min available is v1.3.400
      "org.eclipse.jdt" % "org.eclipse.jdt.core" % "3.19.0",

      scalatest
    ) ++ logback
  )
  .dependsOn(clientLib)