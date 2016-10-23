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
        appsetting => onSuccess(appSettingRepository.saveAppSettings(appsetting)) {
          case Some(response) => complete(StatusCodes.OK,response)
          case None => complete(StatusCodes.BadRequest,"Unable to save App Settings")
        }
      }
    }
  }
  val getAppSettings = get {
    path("config") {
      val appSettings = appSettingRepository.getAppSettings()
      onSuccess(appSettings){
        case Some(response) => complete(StatusCodes.OK,response)
        case None => complete(StatusCodes.BadRequest , "Unable to fetch App Settings")
      }

    }
  }
  val addSetting = post {
    path(PathMatchers.separateOnSlashes("config/settings")) {
      entity(as[Map[String, String]]) {
        setting =>
          val response = appSettingRepository.saveSetting(setting)
          onSuccess(response){
            case Some(respo) => complete(StatusCodes.OK,respo)
            case None => complete(StatusCodes.BadRequest,"Unable to save settings")
          }
      }
    }
  }

  val getSettings = path(PathMatchers.separateOnSlashes("config/settings")) {
    get {
      val appSettings = appSettingRepository.getSettings()
      onSuccess(appSettings){
        case Some(response) => complete(StatusCodes.OK,response)
        case None => complete(StatusCodes.BadRequest,"Unable to get Settings")
      }
    }
  }

  val deleteSettings = path(PathMatchers.separateOnSlashes("config/settings")) {
    delete {
      entity(as[List[String]]) { list =>
        val isDeleted = appSettingRepository.deleteSettings(list)
        onSuccess(isDeleted){
          case Some(response) => complete(StatusCodes.OK, response)
          case None => complete(StatusCodes.BadRequest,"Unable to delete settings")
         }
      }
    }
  }

  val updateLogLevel = path(PathMatchers.separateOnSlashes("config/logs/level")) {
    put {
      entity(as[String]) {
        level =>
          val res = logLevelUpdater.setLogLevel(logLevelUpdater.ROOT, level)
          onSuccess(res){
            case Some(response) => complete(StatusCodes.OK,response)
            case None => complete(StatusCodes.BadRequest , "Unable to update the log level")
          }

      }
    }
  }
  val getLogLevel = path(PathMatchers.separateOnSlashes("config/logs/level")) {
    get {
      val loglevel = logLevelUpdater.getLogLevel(logLevelUpdater.ROOT)
      onSuccess(loglevel){
        case Some(response) => complete(StatusCodes.OK, response)
        case None => complete(StatusCodes.BadRequest , "Unable get log level")
      }

    }
  }

  val index = path(""){
    getFromResource("web/index.html")
  }


}
