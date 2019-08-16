import BuildSettings._
import Dependencies._

lazy val pixels = (project in file("."))
  .settings(pixelsBuildSettings: _*)
  .aggregate(api)

lazy val api = (project in file("api"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      akkaHttp,
      akka,
      akkaTyped,
      akkaStream,
      akkaStreamTestkit,
      akkaHttpTestkit
    )
  )
  .dependsOn(protobuf)

lazy val protobuf = (project in file("protobuf"))
  .enablePlugins(AkkaGrpcPlugin)
  .settings(pixelsBuildSettings: _*)
