import sbt._

object Dependencies {
  lazy val scalatest = "org.scalatest" %% "scalatest" % Version.scalatest
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  lazy val akka = "com.typesafe.akka" %% "akka-actor" % Version.akka
  lazy val akkaTyped = "com.typesafe.akka" %% "akka-actor-typed" % Version.akka
  lazy val akkaTypedClusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % Version.akka
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  lazy val akkaStreamTyped = "com.typesafe.akka" %% "akka-stream-typed" % Version.akka
  lazy val akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % Version.akka % Test
  lazy val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % Test
  lazy val alpakkaS3 = "com.lightbend.akka" %% "akka-stream-alpakka-s3" % Version.alpakka
  lazy val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.akkaPersistenceCassandra
  lazy val cassandraLauncher = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % Version.akkaPersistenceCassandra % Test
}
