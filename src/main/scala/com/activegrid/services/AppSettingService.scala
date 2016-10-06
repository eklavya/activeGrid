package com.activegrid.services


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.activegrid.models.{AppSettingRepository, AppSettings, JsonSupport, Setting}
import com.typesafe.config.ConfigFactory
import jdk.net.SocketFlow.Status
import org.slf4j.LoggerFactory

import scala.concurrent.Future


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
      entity(as[Map[String,String]]) {
        setting =>
          appSettingRepository.saveSetting(setting)
          complete("Done")
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
      entity(as[List[String]]) { list =>
        appSettingRepository.deleteSettings(list)
        complete("Done")
      }
    }
  }

  val routes = addAppSetting  ~ addSetting ~ getAppSettings~deleteSettings~getSettings

  Http().bindAndHandle(routes, config.getString("http.host"), config.getInt("http.port"))

  println("Server Running")

}
