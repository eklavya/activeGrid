package com.activegrid.services


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers
import com.activegrid.models.{AppSettingRepository, AppSettings, JsonSupport, LogLevelUpdater}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext


class AppSettingService(implicit executionContext: ExecutionContext) extends JsonSupport {
  val logger = LoggerFactory.getLogger(getClass)
  val appSettingRepository = new AppSettingRepository
  val logLevelUpdater = new LogLevelUpdater

  val addAppSetting = post {
    path("appsettings") {
      entity(as[AppSettings]) {
        appsetting =>appSettingRepository.saveAppSettings(appsetting)
         complete(StatusCodes.OK,"Done")
      }
    }
  }
  val getAppSettings = get {
    path("config") {
      val appSettings = appSettingRepository.getAppSettings()
      onComplete(appSettings) {
        case util.Success(response) => complete(StatusCodes.OK, response)
        case util.Failure(exception) => complete(StatusCodes.BadRequest,"Unable to get App Settings")
      }

    }
  }
  val addSetting = post {
    path(PathMatchers.separateOnSlashes("config/settings")) {
      entity(as[Map[String, String]]) {
        setting =>
          appSettingRepository.saveSetting(setting)
          complete(StatusCodes.OK, "Done")
      }
    }
  }

  val getSettings = path(PathMatchers.separateOnSlashes("config/settings")) {
    get {
      val appSettings = appSettingRepository.getSettings()
      onComplete(appSettings) {
        case util.Success(response)  => complete(StatusCodes.OK, response)
        case util.Failure(exception) => complete(StatusCodes.BadRequest, "Unable to fetch the settings")
      }
    }
  }

  val deleteSettings = path(PathMatchers.separateOnSlashes("config/settings")) {
    delete {
      entity(as[List[String]]) { list =>
        val isDeleted = appSettingRepository.deleteSettings(list)
        onComplete(isDeleted) {
          case util.Success(response)  => complete(StatusCodes.OK, "Done")
          case util.Failure(exception) => complete(StatusCodes.BadRequest,"Unable to delete  settings")
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
            case util.Failure(exception) => complete(StatusCodes.BadRequest,"Unable to update log level")
          }

      }
    }
  }
  val getLogLevel = path(PathMatchers.separateOnSlashes("config/logs/level")) {
    get {
      val response = logLevelUpdater.getLogLevel(logLevelUpdater.ROOT)
      onComplete(response) {
       case util.Success(response) =>  complete(StatusCodes.OK, response)
       case util.Failure(exception) => complete(StatusCodes.BadRequest,"Unable to get the log level")
      }

    }
  }

  val index = path(""){
    getFromResource("web/index.html")
  }


}
