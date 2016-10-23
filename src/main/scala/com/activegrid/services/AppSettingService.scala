package com.activegrid.services


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers
import com.activegrid.models.{AppSettingRepository, AppSettings, JsonSupport, LogLevelUpdater}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext


class AppSettingService(implicit executionContext: ExecutionContext) extends JsonSupport {

  val appSettingRepository = new AppSettingRepository
  val logLevelUpdater = new LogLevelUpdater
  override val logger = LoggerFactory.getLogger(getClass)

  val addAppSetting = post {
    path("appsettings") {
      entity(as[AppSettings]) {
        appsetting => appSettingRepository.saveAppSettings(appsetting)
          complete(StatusCodes.OK, "Done")
      }
    }
  }
  val getAppSettings = get {
    path("config") {
      val appSettings = appSettingRepository.getAppSettings()
      onComplete(appSettings) {
        case util.Success(response) => complete(StatusCodes.OK, response)
        case util.Failure(exception) => {
          logger.error(s"Unable to get App Settings")
          complete(StatusCodes.BadRequest, "Unable to get App Settings")
        }
      }

    }
  }
  val addSetting = post {
    path(PathMatchers.separateOnSlashes("config/settings")) {
      entity(as[Map[String, String]]) {
        setting =>
          val resp = appSettingRepository.saveSetting(setting)
          onComplete(resp) {
            case util.Success(response) => complete(StatusCodes.OK, "Done")
            case util.Failure(exception) => {
              logger.error(s"Unable to save the settings $exception")
              complete(StatusCodes.BadRequest, "Unable to save the settings")
            }
          }
      }
    }
  }

  val getSettings = path(PathMatchers.separateOnSlashes("config/settings")) {
    get {
      val appSettings = appSettingRepository.getSettings()
      onComplete(appSettings) {
        case util.Success(response) => complete(StatusCodes.OK, response)
        case util.Failure(exception) => {
          logger.error(s"Unable to fetch settings $exception")
          complete(StatusCodes.BadRequest, "Unable to fetch the settings")
        }
      }
    }
  }

  val deleteSettings = path(PathMatchers.separateOnSlashes("config/settings")) {
    delete {
      entity(as[List[String]]) { list =>
        val isDeleted = appSettingRepository.deleteSettings(list)
        onComplete(isDeleted) {
          case util.Success(response) => complete(StatusCodes.OK, "Done")
          case util.Failure(exception) => {
            logger.error(s"Unable to delete settings $exception")
            complete(StatusCodes.BadRequest, "Unable to delete  settings")
          }
        }
      }
    }
  }

  val updateLogLevel = path(PathMatchers.separateOnSlashes("config/logs/level")) {
    put {
      entity(as[String]) {
        level =>
          val loglevel = logLevelUpdater.setLogLevel(logLevelUpdater.ROOT, level)
          onComplete(loglevel) {
            case util.Success(response) => complete(StatusCodes.OK, "Done")
            case util.Failure(exception) => {
              logger.error(s"Unable to update log level $exception")
              complete(StatusCodes.BadRequest, "Unable to update log level")
            }
          }

      }
    }
  }
  val getLogLevel = path(PathMatchers.separateOnSlashes("config/logs/level")) {
    get {
      val response = logLevelUpdater.getLogLevel(logLevelUpdater.ROOT)
      onComplete(response) {
        case util.Success(response) => complete(StatusCodes.OK, response)
        case util.Failure(exception) => {
          logger.error(s"Unable to get the log level $exception")
          complete(StatusCodes.BadRequest, "Unable to get the log level")
        }
      }
    }
  }

  val index = path("index") {
    getFromResource("web/index.html")
  }

  val appSettingServiceRoutes = addAppSetting ~ addSetting ~ getAppSettings ~ deleteSettings ~ getSettings ~ getLogLevel ~ updateLogLevel ~ index

}
