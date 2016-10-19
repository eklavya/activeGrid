package com.activegrid.controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.activegrid.models.LogConfigUpdater

import scala.concurrent.Future

/**
  * Created by sivag on 7/10/16.
  */
class LogConfigUpdateController {

  val cfgUpdater = new LogConfigUpdater

  val updateRQ = pathPrefix("config") {
    path("logs" / "level") {
      put {
        entity(as[String]) { level =>
          val save: Future[String] = cfgUpdater.setLogLevel(cfgUpdater.ROOT, level)
          onComplete(save) {
            case success => complete(StatusCodes.OK, success)
            case _ => complete(StatusCodes.Accepted, "Unable to update")
          }

        }
      }
    }

  }
  val getRQ = pathPrefix("config") {
    path("logs" / "level") {
      get {
        entity(as[String]) { level =>
          val save: Future[String] = cfgUpdater.getLogLevel(level)
          onComplete(save) {
            case success => complete(StatusCodes.OK, success)
            case _ => complete(StatusCodes.Accepted, "Unable to fetch Log level")
          }

        }
      }
    }

  }

  val routes = updateRQ ~ getRQ


}
