package com.activegrid.services


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.activegrid.models.{AppSettingRepository, AppSettings, JsonSupport, Setting}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory


object AppSettingService extends App with JsonSupport {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val config = ConfigFactory.load()

  val logger = LoggerFactory.getLogger(getClass)
  val appSettingRepository = new AppSettingRepository

  val addAppSetting = post {
    path("appsettings") {
      entity(as[AppSettings]) {
        appsetting => complete {
          appSettingRepository.saveAppSettings(appsetting)
        }
      }
    }
  }
  val getAppSettings = get {
    path("appsettings") {
      complete {
        appSettingRepository.getAppSettings()
      }
    }
  }
  val addSetting = post {
    path("settings") {
      entity(as[List[Setting]]) {
        setting => complete {
          appSettingRepository.saveSetting(setting)
        }
      }
    }
  }

  val getSettings = path("settings") {
    get {
      complete(appSettingRepository.getSettings())
    }
  }

  val deleteSettings = path("settings") {
    delete {
      entity(as[List[Setting]]) { list =>
        appSettingRepository.deleteSettings(list)
        complete("Done")
      }
    }
  }

  val routes = addAppSetting ~ getAppSettings ~ addSetting ~ getAppSettings ~ deleteSettings

  Http().bindAndHandle(routes, config.getString("http.host"), config.getInt("http.port"))

  println("Server Running")

}
