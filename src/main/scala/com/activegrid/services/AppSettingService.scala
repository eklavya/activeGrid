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
        appsetting => onComplete(appSettingRepository.saveAppSettings(appsetting)) {
          done => complete("Done")
        }
      }
    }
  }
  val getAppSettings = get {
    path("config") {
      val appSettings = appSettingRepository.getAppSettings()
      onComplete(appSettings) {
        done => complete(StatusCodes.OK, appSettings)
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
        done => complete(StatusCodes.OK, appSettings)
      }
    }
  }

  val deleteSettings = path(PathMatchers.separateOnSlashes("config/settings")) {
    delete {
      entity(as[List[String]]) { list =>
        val isDeleted = appSettingRepository.deleteSettings(list)
        onComplete(isDeleted) {
          done => complete(StatusCodes.OK, "Done")
        }
      }
    }
  }

  val updateLogLevel = path(PathMatchers.separateOnSlashes("config/logs/level")) {
    put {
      entity(as[String]) {
        level =>
          val res = logLevelUpdater.setLogLevel(logLevelUpdater.ROOT, level)
          onComplete(res) { done =>
            complete(StatusCodes.OK, "")
          }

      }
    }
  }
  val getLogLevel = path(PathMatchers.separateOnSlashes("config/logs/level")) {
    get {
      val response = logLevelUpdater.getLogLevel(logLevelUpdater.ROOT)
      onComplete(response) { done =>
        complete(StatusCodes.OK, response)
      }
    }
  }


}
