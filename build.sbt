name := """activeGrid"""

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.bintrayRepo("hseeberger", "maven"))

libraryDependencies ++= {
  val AkkaVersion       = "2.4.10"
  val Json4sVersion     = "3.2.11"
  val neo4jVersion      = "1.1.0-M01"
  val scalaLoggingVersion = "3.4.0"
  val neo4jScalaVersion = "0.3.3"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % AkkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % AkkaVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % AkkaVersion,
    "com.typesafe.akka" %% "akka-remote" % AkkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % AkkaVersion,
    "com.typesafe.akka" %% "akka-distributed-data-experimental" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-jackson-experimental" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-xml-experimental" % AkkaVersion,
    "ch.qos.logback"    %  "logback-classic" % "1.1.2",
    "org.json4s"        %% "json4s-native"   % Json4sVersion,
    "org.json4s"        %% "json4s-ext"      % Json4sVersion,
    "de.heikoseeberger" %% "akka-http-json4s" % "1.4.2",
    "org.neo4j.driver" % "neo4j-java-driver" % neo4jVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "eu.fakod" % "neo4j-scala_2.11" % neo4jScalaVersion
  )
}