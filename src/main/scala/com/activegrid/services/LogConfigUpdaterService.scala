package com.activegrid.services

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.activegrid.models.LogConfigUpdater

import scala.concurrent.Future

/**
  * Created by sivag on 7/10/16.
  */
class LogConfigUpdaterService {

  val cfgUpdater = new LogConfigUpdater;

  val updateRQ = pathPrefix("config") {
    path("logs" / "level") {
      put {
        entity(as[String]) { level =>
          val save: Future[String] = cfgUpdater.setLogLevel(cfgUpdater.ROOT, level)
          onSuccess(save) {
            case success => complete(StatusCodes.OK, success)
            case none => complete(StatusCodes.BadRequest, "Unable to update")
          }
          /* onComplete(save) { maybeAuthSettings =>
             complete("Done")
           }*/
        }
      }
    }

  }
  val getRQ = pathPrefix("config") {
    path("logs" / "level") {
      get {
        entity(as[String]) { level =>
          val save: Future[String] = cfgUpdater.getLogLevel2(level)
          onSuccess(save) {
            case success => complete(StatusCodes.OK, success)
            case none => complete(StatusCodes.BadRequest, "Unable to fetch Log level")
          }
          /*          onComplete(save) { maybeAuthSettings =>
                      complete("Done")
                    }*/
        }
      }
    }

  }

  val routes = updateRQ ~ getRQ


}
