package com.activegrid.controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.activegrid.models.LogConfigUpdater
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * Created by sivag on 7/10/16.
  */
class LogConfigUpdateController {

  val cfgUpdater = new LogConfigUpdater

  val logger = Logger(LoggerFactory.getLogger(getClass.getName));

  val updateRQ = pathPrefix("config") {
    path("logs" / "level") {
      put {
        entity(as[String]) { level =>
          val saved: Future[String] = cfgUpdater.setLogLevel(cfgUpdater.ROOT, level)
          onComplete(saved) {
            case util.Success(saved) => complete(StatusCodes.OK, "Settings saved successfully")
            case util.Failure(ex) =>
              logger.error("ERROR WHILE UPDATING LOG LEVEL", ex)
              complete("ERROR  WHILE UPDATING LOG LEVEL")
          }

        }
      }
    }

  }
  val getRQ = pathPrefix("config") {
    path("logs" / "level") {
      get {
        entity(as[String]) { level =>
          val loglevel: Future[String] = cfgUpdater.getLogLevel(level)
          onComplete(loglevel) {
            case util.Success(loglevel) => complete(StatusCodes.OK, "Settings saved successfully")
            case util.Failure(ex) =>
              logger.error("ERROR WHILE GETTING LOG SETTINGS", ex)
              complete("ERROR  WHILE GETTING LOG SETTINGS")
          }

        }
      }
    }

  }
  val routes = updateRQ ~ getRQ


}
