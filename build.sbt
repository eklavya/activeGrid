 name := """activeGrid"""

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.bintrayRepo("hseeberger", "maven"))

libraryDependencies ++= {
  val AkkaVersion       = "2.4.10"
  val scalaLoggingVersion = "3.4.0"
  val neo4jScalaVersion = "0.3.3"
  val log4jVersion      = "1.2.17"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-jackson-experimental" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % AkkaVersion,
    "ch.qos.logback"    %  "logback-classic" % "1.1.2",
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "eu.fakod" % "neo4j-scala_2.11" % neo4jScalaVersion,
  "log4j" % "log4j" % log4jVersion
  )
}