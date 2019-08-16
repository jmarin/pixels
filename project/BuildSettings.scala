import sbt.Keys._
import sbt._
import Dependencies._

object BuildSettings {
  val buildScalaVersion = "2.13.0"
  val buildVersion = "1.0"

  val pixelsBuildSettings = Defaults.coreDefaultSettings ++
    Seq(
      version := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq(
        "-Xlint",
        "-deprecation",
        "-unchecked",
        "-feature"
      ),
      parallelExecution in Test := true,
      fork in Test := true,
      libraryDependencies ++= Seq(scalatest)
    )
}
