package com.activegrid


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer




object Main extends App {

  implicit val system  = ActorSystem();
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher;

  var apiHandler = new RouteDefinition();

  val binding = Http().bindAndHandle(handler = apiHandler.catalogRoute, interface = "localhost", port = 5000);

}
