import BuildSettings._
import Dependencies._

lazy val pixels = (project in file("."))
  .settings(pixelsBuildSettings: _*)
  .aggregate(api)

lazy val api = (project in file("api"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= Seq(akkaHttp, akka, akkaTyped, akkaStream, akkaStreamTyped)
  )

lazy val protobuf = (project in file("protobuf"))
  .settings(pixelsBuildSettings: _*)
