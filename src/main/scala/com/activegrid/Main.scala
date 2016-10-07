package com.activegrid

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.activegrid.services.AppSettingService
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory


object Main extends App {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val appSettingService : AppSettingService = new AppSettingService

  val routes = appSettingService.addAppSetting ~ appSettingService.addSetting ~ appSettingService.getAppSettings ~ appSettingService.deleteSettings ~ appSettingService.getSettings ~ appSettingService.getLogLevel ~ appSettingService.updateLogLevel
  Http().bindAndHandle(routes, "localhost", 8000)
  logger.info(s"Server online at http://localhost:8000")

}

