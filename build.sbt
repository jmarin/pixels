import BuildSettings._
import Dependencies._

lazy val pixels = (project in file("."))
  .settings(pixelsBuildSettings: _*)
  .aggregate(api, `pixels-management`)

lazy val api = (project in file("api"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      akkaHttp,
      akka,
      akkaTyped,
      akkaStream,
      akkaStreamTestkit,
      akkaHttpTestkit,
      alpakkaS3
    )
  )
  .dependsOn(protobuf)

lazy val protobuf = (project in file("protobuf"))
  .enablePlugins(AkkaGrpcPlugin)
  .settings(pixelsBuildSettings: _*)

lazy val `pixels-management` = (project in file("pixels-management"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      akkaTypedClusterSharding
    )
  )
