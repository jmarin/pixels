import BuildSettings._
import Dependencies._

lazy val pixels = (project in file("."))
  .settings(pixelsBuildSettings: _*)
  .aggregate(management, metadata)

lazy val commonDeps = Seq(logback, scalatest, scalacheck)

lazy val common = (project in file("common"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      commonsImaging,
      imageScaling,
      akkaTyped,
      akkaTypedClusterSharding,
      akkaStream,
      alpakkaS3,
      akkaStreamTestkit,
      akkaPersistenceCassandra
    )
  )

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
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(metadata % "compile->compile;test->test")

lazy val metadata = (project in file("metadata"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      akkaTypedClusterSharding,
      akkaPersistenceInMemory,
      akkaPersistenceCassandra
    )
  )
  .dependsOn(common % "compile->compile;test->test")
