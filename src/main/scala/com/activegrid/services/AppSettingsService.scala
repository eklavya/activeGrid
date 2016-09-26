package com.activegrid.services

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.activegrid.utils.Utils
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.activegrid.entities.{AuthSettings, Software}
import persistance.AppSettingsManager
import spray.json.DefaultJsonProtocol._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import  com.activegrid.entities.AuthSettings;

/**
  * Created by sivag on 26/9/16.
  */
class AppSettingsService {

       val persistance = new AppSettingsManager;

       implicit val authSettings = jsonFormat3(AuthSettings);

       val  routes = pathPrefix("config") {
           path("settings"/"auth") {
             post {
               entity(as[AuthSettings]) { authSettings =>
                 val save: Future[AuthSettings] = persistance.persistAuthSettings(authSettings)
                 onComplete(save) {
                   complete("To insert app settings")
                 }
               }
             }
           } ~
             put {
               entity(as[AuthSettings]) { authSettings =>
                 val save: Future[AuthSettings] = persistance.persistAuthSettings(authSettings)
                 onComplete(save) {
                   complete("To update app settings")
                 }
             } ~
             get {
               entity(as[List[AuthSettings]]) { result =>
                  val settingsList: Future[List[AuthSettings]] =  persistance.getSettings()
                 onComplete(settingsList) { done =>
                  settingsList
                 }
               }
             } ~
             delete {
               entity(as[String]) { result: String =>
                 val save: Future[String] = Future {
                   "Not implemented"
                 }
                 onComplete(save) { done =>
                   complete("Delete app settings")
                 }
               }
             }
       }
}
