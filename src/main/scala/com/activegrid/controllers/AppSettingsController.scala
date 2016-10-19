package com.activegrid.controllers


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.activegrid.entities.AppSettings
import com.activegrid.models.AppSettingImpl
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future

/**
  * Created by sivag on 26/9/16.
  * //StatusCode = Accepted (202), denotes request accepted but failed to do perform required operation.
  */


class AppSettingsController {

  val persistanceMgr = new AppSettingImpl

  implicit val appSettings = jsonFormat2(AppSettings)

  val postRQ = pathPrefix("config") {
    path("settings") {
      post {
        entity(as[AppSettings]) { appSettings =>
          val save: Future[String] = persistanceMgr.addSettings(appSettings)
          onComplete(save) {
            case success => complete(StatusCodes.OK, "Settings saved successfully")
            case _ => complete(StatusCodes.Accepted, "Saving operation failed.")
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
            case success => complete(StatusCodes.OK, "Settings updated successfully")
            case _ => complete(StatusCodes.Accepted, "Update operation failed.")
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
            case success => complete(StatusCodes.OK, "Authsettings updated successfully")
            case _ => complete(StatusCodes.Accepted, "Authsetting updation  failed.")
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
            case success => complete(StatusCodes.OK, "Settings updated successfully")
            case _ => complete(StatusCodes.Accepted, "Delete operation failed.")
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
            case success => complete(StatusCodes.OK, "Operation succesfull")
            case _ => complete(StatusCodes.Accepted, "Delete operation failed.")
          }
        }
      }
    }

  }
  val routes = getRQ ~ postRQ ~ updateRQ ~ updateAuthRQ ~ deleteRQ ~ deleteAuthRQ
}
