package com.activegrid


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.activegrid.entities.AppSettings
import com.activegrid.models.{AppSettingWrapper, LogConfigUpdater}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

import scala.util.{Failure, Success}


object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher


  val appSettingsWrapper = new AppSettingWrapper
  val cfgUpdater = new LogConfigUpdater
  implicit val appSettings = jsonFormat2(AppSettings)
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val settingsPostRQ = pathPrefix("config") {
    path("settings") {
      post {
        entity(as[AppSettings]) { appSettings =>
          val maybeAdded = appSettingsWrapper.addSettings(appSettings)
          onComplete(maybeAdded) {
            case Success(save) => complete(StatusCodes.OK, "Settings saved successfully")
            case Failure(ex) =>
              logger.error("Error while save settings",ex)
              complete(StatusCodes.InternalServerError, "These is problem while processing request")
          }

        }
      }
    }
  }

  val settingsUpdateRQ = pathPrefix("config") {
    path("settings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeUpdated = appSettingsWrapper.updateSettings(appSettings)
          onComplete(maybeUpdated) {
            case Success(update)  => update.status match {
              case true  => complete(StatusCodes.OK,"Updated successfully")
              case false => complete(StatusCodes.OK,"Updated failed,,Retry!!")
            }
            case Failure(ex) =>
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
  val settingsUpdateAuthRQ = pathPrefix("config") {
    path("authsettings") {
      put {
        entity(as[Map[String, String]]) { appSettings =>
          val maybeUpdated = appSettingsWrapper.updateAuthSettings(appSettings)
          onComplete(maybeUpdated) {
            case Success(update)  => update.status match {
              case true  => complete(StatusCodes.OK,"Updated successfully")
              case false => complete(StatusCodes.OK,"Updated failed,,Retry!!")
            }
            case Failure(ex) =>
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
            case Success(delete)  => delete.status match {
              case true  => complete(StatusCodes.OK,"Deleted successfully")
              case false => complete(StatusCodes.OK,"Deletion failed,,Retry!!")
            }
            case Failure(ex) =>
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
  val settingGetRQ = pathPrefix("config") {
    path("settings") {
      get {
        val allSettings = appSettingsWrapper.getSettings()
        onComplete(allSettings) {
          case Success(settings) =>
            complete(StatusCodes.OK, settings)
          case Failure(ex) =>
            logger.error("Failed to get settings", ex)
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
            case Success(delete)  => delete.status match {
              case true  => complete(StatusCodes.OK,"Deleted successfully")
              case false => complete(StatusCodes.OK,"Deletion failed,,Retry!!")
            }
            case Failure(ex) =>
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

  val logUpdateRQ = pathPrefix("config") {
    path("logs" / "level") {
      put {
        entity(as[String]) { level =>
          val saved = cfgUpdater.setLogLevel(cfgUpdater.ROOT, level)
          onComplete(saved) {
            case Success(saved) => complete(StatusCodes.OK, "Settings saved successfully")
            case Failure(ex) =>
              logger.error("ERROR WHILE UPDATING LOG LEVEL", ex)
              complete("ERROR  WHILE UPDATING LOG LEVEL")
          }

        }
      }
    }

  }
  val logGetRQ = pathPrefix("config") {
    path("logs" / "level") {
      get {
        entity(as[String]) { level =>
          val loglevel = cfgUpdater.getLogLevel(level)
          onComplete(loglevel) {
            case Success(loglevel) => complete(StatusCodes.OK, "Settings saved successfully")
            case Failure(ex) =>
              logger.error("ERROR WHILE GETTING LOG SETTINGS", ex)
              complete("ERROR  WHILE GETTING LOG SETTINGS")
          }

        }
      }
    }

  }
  val routes = logUpdateRQ ~ logGetRQ ~ settingGetRQ ~ settingsPostRQ ~ settingsUpdateRQ ~ settingsUpdateAuthRQ ~ deleteRQ ~ deleteAuthRQ


  Http().bindAndHandle(handler = routes, interface = "localhost", port = 5000)

}
