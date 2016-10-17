package com.activegrid.services


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.activegrid.entities.AppSettings
import com.activegrid.models.AppSettingImpl
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future

/**
  * Created by sivag on 26/9/16.
  */
class AppSettingsService {

  val persistanceMgr = new AppSettingImpl

  implicit val appSettings = jsonFormat2(AppSettings)

  val postRQ = pathPrefix("config") {
    path("settings") {
      post {
        entity(as[AppSettings]) { appSettings =>
          val save: Future[String] = persistanceMgr.addSettings(appSettings)
          onSuccess(save) {
            case success => complete(StatusCodes.OK, "Settings saved successfully")
            case fail => complete(StatusCodes.BadRequest, "Saving operation failed.")
          }
          /*onComplete(save) { maybeAuthSettings =>
            complete("Done")
          }*/
        }
      }
    }

  }

  val updateRQ = pathPrefix("config") {
    path("settings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val save: Future[String] = persistanceMgr.updateSettings(appSettings)
          onSuccess(save) {
            case success => complete(StatusCodes.OK, "Settings updated successfully")
            case fail => complete(StatusCodes.BadRequest, "Update operation failed.")
          }
          /*onComplete(save) { maybeAuthSettings =>
            complete(save)
          }*/
        }
      }
    }

  }
  val updateAuthRQ = pathPrefix("config") {
    path("authsettings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val save: Future[String] = persistanceMgr.updateAuthSettings(appSettings)
          onSuccess(save) {
            case success => complete(StatusCodes.OK, "Authsettings updated successfully")
            case fail => complete(StatusCodes.BadRequest, "Authsetting updation  failed.")
          }
          /*onComplete(save) { maybeAuthSettings =>
            complete(save)
          }*/

        }
      }
    }

  }


  val deleteRQ = pathPrefix("config") {
    path("settings") {
      delete {
        entity(as[Map[String, String]]) { appSettings =>
          val save: Future[String] = persistanceMgr.deleteSettings(appSettings)
          onSuccess(save) {
            case success => complete(StatusCodes.OK, "Settings updated successfully")
            case fail => complete(StatusCodes.BadRequest, "Delete operation failed.")
          }
          /* onComplete(save) { maybeAuthSettings =>
             complete(save)
           }*/
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
          onSuccess(save) {
            case success => complete(StatusCodes.OK, "Operation succesfull")
            case fail => complete(StatusCodes.BadRequest, "Delete operation failed.")
          }
          /* onComplete(save) { maybeAuthSettings =>
             complete(save)
           }*/
        }
      }
    }

  }
  val routes = getRQ ~ postRQ ~ updateRQ ~ updateAuthRQ ~ deleteRQ ~ deleteAuthRQ
}
