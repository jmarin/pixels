import BuildSettings._
import Dependencies._

lazy val pixels = (project in file("."))
  .settings(pixelsBuildSettings: _*)
  .aggregate(common, api)

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
  .dependsOn(common)

lazy val management = (project in file("management"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      akkaTypedClusterSharding,
      akkaStream,
      akkaPersistenceInMemory,
      akkaHttp,
      akkaHttpTestkit
    )
  )
