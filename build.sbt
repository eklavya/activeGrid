name := """activeGrid"""

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.bintrayRepo("hseeberger", "maven"),"AWS" at "https://mvnrepository.com/artifact/org.jclouds.provider/aws-ec2","AWS3" at "https://mvnrepository.com/artifact/org.jclouds.provider/aws-s3")

libraryDependencies ++= {
  val AkkaVersion       = "2.4.10"
  val scalaLoggingVersion = "3.4.0"
  val neo4jScalaVersion = "0.3.3"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % AkkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "eu.fakod" % "neo4j-scala_2.11" % neo4jScalaVersion,
    "org.apache.jclouds.provider" % "aws-ec2" % "1.6.3",
    "org.apache.jclouds.provider" % "aws-s3" % "1.6.3",
    "com.amazonaws" % "aws-java-sdk" % "1.11.46",
    "org.scalastyle" %% "scalastyle" % "0.8.0"
  )
}
