import BuildSettings._
import Dependencies._

lazy val pixels = (project in file("."))
  .settings(pixelsBuildSettings: _*)
  .aggregate(management, metadata, search)

lazy val commonDeps = Seq(logback, scalatest, scalacheck)

lazy val common = (project in file("common"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      commonsImaging,
      akkaHttp,
      akkaStream,
      slick,
      slickHikaricp
    )
  )

lazy val management = (project in file("management"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      akkaTyped,
      akkaTypedClusterSharding,
      akkaStream,
      akkaPersistenceInMemory,
      akkaPersistenceCassandra,
      akkaHttp,
      akkaHttpTestkit,
      imageScaling,
      postgres,
      h2
    )
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(metadata % "compile->compile;test->test")

lazy val metadata = (project in file("metadata"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      commonsImaging
    )
  )
  .dependsOn(common % "compile->compile;test->test")

lazy val search = (project in file("search"))
  .settings(pixelsBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ Seq(
      akkaTypedPersistence,
      akkaPersistenceInMemory,
      akkaPersistenceCassandra,
      akkaPersistenceQuery,
      akkaTyped,
      akkaHttp,
      akkaStream,
      akkaStreamTyped
    )
  )
