package com.activegrid


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.activegrid.utils.EndpointsAggregator



object  Main extends App {

  implicit val system  = ActorSystem();
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher;

  Http().bindAndHandle(handler = EndpointsAggregator.endPoints, interface = "localhost", port = 5000)
}
