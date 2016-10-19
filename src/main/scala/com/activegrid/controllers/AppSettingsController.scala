package com.activegrid.controllers


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.activegrid.entities.AppSettings
import com.activegrid.models.AppSettingImpl
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future

/**
  * Created by sivag on 26/9/16.
  * //StatusCode = Accepted (202), denotes request accepted but failed to do perform required operation.
  */


class AppSettingsController {

  val persistanceMgr = new AppSettingImpl
  implicit val appSettings = jsonFormat2(AppSettings)
  val logger = Logger(LoggerFactory.getLogger(getClass.getName));

  val postRQ = pathPrefix("config") {
    path("settings") {
      post {
        entity(as[AppSettings]) { appSettings =>
          val save: Future[String] = persistanceMgr.addSettings(appSettings)
          onComplete(save) {
            case util.Success(save) =>  complete(StatusCodes.OK, "Settings saved successfully")
            case util.Failure(save) => complete("Error while saving settings")
          }

        }
      }
    }

  }

  val updateRQ = pathPrefix("config") {
    path("settings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val save: Future[String] = persistanceMgr.updateSettings(appSettings)
          onComplete(save) {
            case util.Success(save) => complete(StatusCodes.OK, "Settings updated successfully")
            case util.Failure(save) => complete(StatusCodes.BadRequest,"Failed to update settings")
          }
        }
      }
    }

  }
  val updateAuthRQ = pathPrefix("config") {
    path("authsettings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val save: Future[String] = persistanceMgr.updateAuthSettings(appSettings)
          onComplete(save) {
            case util.Success(save) => complete(StatusCodes.OK, "Authsettings updated successfully")
            case util.Failure(save) => complete(StatusCodes.BadRequest, "Authsetting updation  failed.")
          }
        }
      }
    }

  }
  val deleteRQ = pathPrefix("config") {
    path("settings") {
      delete {
        entity(as[Map[String, String]]) { appSettings =>
          val save: Future[String] = persistanceMgr.deleteSettings(appSettings)
          onComplete(save) {
            case util.Success(save) => complete(StatusCodes.OK, "Settings deleted successfully")
            case util.Failure(save) => complete(StatusCodes.BadRequest, "Delete operation failed.")
          }
        }
      }
    }
  }
  val getRQ = pathPrefix("config") {
    path("settings") {
      get {
        complete(persistanceMgr.getSettings())
      }
    }
  }
  val deleteAuthRQ = pathPrefix("config") {
    path("authsettings") {
      delete {
        entity(as[Map[String, String]]) { appSettings =>
          val save: Future[String] = persistanceMgr.deleteAuthSettings(appSettings)
          onComplete(save) {
            case util.Success(save) => complete(StatusCodes.OK, "Operation succesfull")
            case util.Failure(save) => complete(StatusCodes.BadRequest, "Delete operation failed.")
          }
        }
      }
    }

  }
  val routes = getRQ ~ postRQ ~ updateRQ ~ updateAuthRQ ~ deleteRQ ~ deleteAuthRQ
}
