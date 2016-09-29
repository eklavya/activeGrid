package com.activegrid.services


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.slf4j.LoggerFactory
import akka.http.scaladsl.server.Directives._
import com.activegrid.entities.{AppSettings, JsonSupport, Setting}
import com.activegrid.repositories._


object AppSettingService extends App with JsonSupport {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val logger = LoggerFactory.getLogger(getClass)
  val appSettingRepository = new AppSettingRepository

  val addSetting = post {
    path("settings") {
      entity(as[List[Setting]]) {
        setting => complete {
          appSettingRepository.saveSetting(setting)
        }
      }
    }
  }

  val getAppSettings = path("settings") {
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

  val routes = addSetting ~ getAppSettings ~ deleteSettings

  Http().bindAndHandle(routes, "localhost", 9000)

  println("Server Running")

}
