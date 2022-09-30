ThisBuild / scalaVersion     := "2.12.15"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "edu.duke.cs.apex"

val chiselVersion = "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "chisel-torch",
    libraryDependencies ++= Seq(
      "edu.berkeley.cs" %% "chisel3" % chiselVersion,
      "edu.berkeley.cs" %% "chiseltest" % "0.5.1" % "test",
      "edu.berkeley.cs" %% "chisel-iotesters" % "2.5.4",
      "edu.berkeley.cs" %% "hardfloat" % "1.5-SNAPSHOT" 
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-P:chiselplugin:genBundleElements",
    ),
    addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full),
  )

