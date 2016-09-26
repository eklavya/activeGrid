package com.activegrid


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.activegrid.services.{AppSettingsService, CatalogService}
import akka.http.scaladsl.server.Directives._

object EndpointsAggregator {
  val appService = new AppSettingsService()
  val catalogService = new CatalogService()
  val endPoints = appService.routes ~ catalogService.routes
}


object Main extends App {

  implicit val system  = ActorSystem();
  implicit val materializer = ActorMaterializer()

  implicit val executionContext = system.dispatcher;

  val binding = {
    Http().bindAndHandle(handler = EndpointsAggregator.endPoints, interface = "localhost", port = 5000)
  };

}
