package com.activegrid.controllers


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.activegrid.entities.AppSettings
import com.activegrid.models.{AppSettingWrapper, ExecutionStatus}
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
          val maybeAdded = appSettingsWrapper.addSettings(appSettings)
          onComplete(maybeAdded) {
            case util.Success(save) => complete(StatusCodes.OK, "Settings saved successfully")
            case util.Failure(ex) =>
              logger.error("Error while save settings", ex)
              complete(StatusCodes.InternalServerError, "These is problem while processing request")
          }

        }
      }
    }
  }

  val updateRQ = pathPrefix("config") {
    path("settings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeUpdated = appSettingsWrapper.updateSettings(appSettings)
          onComplete(maybeUpdated) {
            case util.Success(update)  => update.status match {
              case true  => complete(StatusCodes.OK,"Updated successfully")
              case false => complete(StatusCodes.OK,"Updated failed,,Retry!!")
            }
            case util.Failure(ex) =>
              ex match {
                case aie:IllegalArgumentException => complete(StatusCodes.OK,"Failed to update settings")
                case _ => complete(StatusCodes.InternalServerError, "These is problem while processing request")
              }
          }
        }
      }
    }

  }
  val updateAuthRQ = pathPrefix("config") {
    path("authsettings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeUpdated = appSettingsWrapper.updateAuthSettings(appSettings)
          onComplete(maybeUpdated) {
            case util.Success(update)  => update.status match {
              case true  => complete(StatusCodes.OK,"Updated successfully")
              case false => complete(StatusCodes.OK,"Updated failed,,Retry!!")
            }
            case util.Failure(ex) =>
              ex match {
                case aie:IllegalArgumentException =>
                  logger.error("Update operation failed", ex)
                  complete(StatusCodes.OK,"Failed to update settings")
                case _ =>
                  logger.error("Update operation failed", ex)
                  complete(StatusCodes.InternalServerError, "These is problem while processing request")
              }
          }
        }
      }
    }

  }
  val deleteRQ = pathPrefix("config") {
    path("settings") {
      delete {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeDeleted = appSettingsWrapper.deleteSettings(appSettings)
          onComplete(maybeDeleted) {
            case util.Success(delete)  => delete.status match {
              case true  => complete(StatusCodes.OK,"Deleted successfully")
              case false => complete(StatusCodes.OK,"Deletion failed,,Retry!!")
            }
            case util.Failure(ex) =>
              ex match {
                case aie:IllegalArgumentException =>
                  logger.error("Delete operation failed", ex)
                  complete(StatusCodes.OK,"Failed to delete settings")
                case _ =>
                  logger.error("Delete operation failed", ex)
                  complete(StatusCodes.InternalServerError, "These is problem while processing request")
              }
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
          val maybeDelete = appSettingsWrapper.deleteAuthSettings(appSettings)
          onComplete(maybeDelete) {
            case util.Success(delete)  => delete.status match {
              case true  => complete(StatusCodes.OK,"Deleted successfully")
              case false => complete(StatusCodes.OK,"Deletion failed,,Retry!!")
            }
            case util.Failure(ex) =>
              ex match {
                case aie:IllegalArgumentException =>
                  logger.error("Delete operation failed", ex)
                  complete(StatusCodes.OK,"Failed to delete settings")
                case _ =>
                  logger.error("Delete operation failed", ex)
                  complete(StatusCodes.InternalServerError, "These is problem while processing request")
              }
          }
        }
      }
    }

  }
  val routes = getRQ ~ postRQ ~ updateRQ ~ updateAuthRQ ~ deleteRQ ~ deleteAuthRQ
}
