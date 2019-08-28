import BuildSettings._
import Dependencies._

lazy val pixels = (project in file("."))
  .settings(pixelsBuildSettings: _*)
  .aggregate(common, api, `pixels-management`, `metadata-extractor`)

lazy val commonDeps = Seq(logback, scalatest, scalacheck)

lazy val common = (project in file("common"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      akkaTyped,
      akkaTypedClusterSharding,
      akkaStream,
      alpakkaS3,
      akkaStreamTestkit
    )
  )

lazy val api = (project in file("api"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      akkaHttp,
      akka,
      akkaHttpTestkit
    )
  )
  .dependsOn(protobuf, common)

lazy val protobuf = (project in file("protobuf"))
  .enablePlugins(AkkaGrpcPlugin)
  .settings(pixelsBuildSettings: _*)

lazy val `pixels-management` = (project in file("pixels-management"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      akkaPersistenceInMemory
    )
  )
  .dependsOn(common)

lazy val `metadata-extractor` = (project in file("metadata-extractor"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      commonsImaging
    )
  )
