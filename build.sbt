name := """activeGrid"""

version := "1.0"

scalaVersion := "2.11.8"


libraryDependencies ++= Seq(

  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.10",
  "com.typesafe.akka" %% "akka-cluster" % "2.4.10",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.10",
  "com.typesafe.akka" %% "akka-cluster-tools" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.10",
  "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.4.10",
  "com.typesafe.akka" %% "akka-remote" % "2.4.10",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.10",
  "com.typesafe.akka" %% "akka-stream" % "2.4.10",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.10",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.10",
  "com.typesafe.akka" %% "akka-distributed-data-experimental" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-jackson-experimental" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.10",
  "com.typesafe.akka" %% "akka-http-xml-experimental" % "2.4.10",
  "ch.qos.logback" %  "logback-classic" % "1.1.7",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0"
)