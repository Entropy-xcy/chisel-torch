ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "edu.duke.cs.apex"

val chiselVersion = "3.5.4"

lazy val root = (project in file("."))
    .settings(
        name := "chiseltorch",
        libraryDependencies ++= Seq(
            "edu.berkeley.cs" %% "chisel3" % chiselVersion,
            "edu.berkeley.cs" %% "chiseltest" % "0.5.1" % "test",
            "edu.berkeley.cs" %% "hardfloat" % "1.5-SNAPSHOT"
        ),
        scalacOptions ++= Seq(
            "-Xcheckinit",
            "-P:chiselplugin:genBundleElements",
            "-deprecation",
            "-encoding", "UTF-8",
            "-feature",
            "-unchecked",
            "-Xfatal-warnings",
            "-language:reflectiveCalls",
            "-Ymacro-annotations"
        ),
        addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
    )

libraryDependencies ++= {
    val liftVersion = "3.5.0" // Put the current/latest lift version here
    Seq(
        "net.liftweb" %% "lift-json" % liftVersion % "compile->default")
}