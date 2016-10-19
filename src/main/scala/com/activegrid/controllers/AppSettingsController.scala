package com.activegrid.controllers


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.activegrid.entities.AppSettings
import com.activegrid.models.AppSettingWrapper
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future

/**
  * Created by sivag on 26/9/16.
  * //StatusCode = Accepted (202), denotes request accepted but failed to do perform required operation.
  */


class AppSettingsController {

  val appSettingsWrapper = new AppSettingWrapper
  implicit val appSettings = jsonFormat2(AppSettings)
  val logger = Logger(LoggerFactory.getLogger(getClass.getName));

  val postRQ = pathPrefix("config") {
    path("settings") {
      post {
        entity(as[AppSettings]) { appSettings =>
          val maybeAdded: Future[String] = appSettingsWrapper.addSettings(appSettings)
          onComplete(maybeAdded) {
            case util.Success(save) => complete(StatusCodes.OK, "Settings saved successfully")
            case util.Failure(ex) =>
              logger.error("Error while save settings", ex)
              complete("Error while saving settings")
          }

        }
      }
    }

  }

  val updateRQ = pathPrefix("config") {
    path("settings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeUpdated: Future[String] = appSettingsWrapper.updateSettings(appSettings)
          onComplete(maybeUpdated) {
            case util.Success(save) => complete(StatusCodes.OK, "Settings updated successfully")
            case util.Failure(ex) =>
              logger.error("Failed to update settings", ex)
              complete(StatusCodes.BadRequest, "Failed to update settings")
          }
        }
      }
    }

  }
  val updateAuthRQ = pathPrefix("config") {
    path("authsettings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeUpdated: Future[String] = appSettingsWrapper.updateAuthSettings(appSettings)
          onComplete(maybeUpdated) {
            case util.Success(save) => complete(StatusCodes.OK, "Authsettings updated successfully")
            case util.Failure(ex) =>
              logger.error("Failed to update settings", ex)
              complete(StatusCodes.BadRequest, "Authsetting updation  failed.")
          }
        }
      }
    }

  }
  val deleteRQ = pathPrefix("config") {
    path("settings") {
      delete {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeDeleted: Future[String] = appSettingsWrapper.deleteSettings(appSettings)
          onComplete(maybeDeleted) {
            case util.Success(save) => complete(StatusCodes.OK, "Settings deleted successfully")
            case util.Failure(ex) =>
              logger.error("Delete operation failed", ex)
              complete(StatusCodes.BadRequest, "Delete operation failed.")
          }
        }
      }
    }
  }
  val getRQ = pathPrefix("config") {
    path("settings") {
      get {
        val allSettings: Future[AppSettings] = appSettingsWrapper.getSettings()
        onComplete(allSettings) {
          case util.Success(settings) => complete("Success", settings)
          case util.Failure(ex) => logger.error("Failed to get settings", ex)
            complete("Failed to get settings")
        }
      }
    }
  }
  val deleteAuthRQ = pathPrefix("config") {
    path("authsettings") {
      delete {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeDelete: Future[String] = appSettingsWrapper.deleteAuthSettings(appSettings)
          onComplete(maybeDelete) {
            case util.Success(save) => complete(StatusCodes.OK, "Deleted  succesfully")
            case util.Failure(ex) =>
              logger.error("Delete operation failed", ex)
              complete(StatusCodes.BadRequest, "Delete operation failed.")
          }
        }
      }
    }

  }
  val routes = getRQ ~ postRQ ~ updateRQ ~ updateAuthRQ ~ deleteRQ ~ deleteAuthRQ
}
