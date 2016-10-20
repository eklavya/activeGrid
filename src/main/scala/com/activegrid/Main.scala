package com.activegrid


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.activegrid.controllers.{AppSettingsController, LogConfigUpdateController}


object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val appCntrl = new AppSettingsController()
  val loggerCntrl = new LogConfigUpdateController()
  val endPoints = appCntrl.routes ~ loggerCntrl.routes

  Http().bindAndHandle(handler = endPoints, interface = "localhost", port = 5000)

}
