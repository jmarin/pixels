import sbt._

object Dependencies {
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  lazy val akka = "com.typesafe.akka" %% "akka-actor" % Version.akka
  lazy val akkaTyped = "com.typesafe.akka" %% "akka-actor-typed" % Version.akka
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  lazy val akkaStreamTyped = "com.typesafe.akka" %% "akka-stream-typed" % Version.akka
}
