package com.activegrid.services


import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.activegrid.utils.Utils
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import com.activegrid.entities.{AuthSettings, Software}
import com.activegrid.persistance.AppSettingsManager
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import com.activegrid.entities.AuthSettings
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}

import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by sivag on 26/9/16.
  */
class AppSettingsService  {

       val persistanceMgr = new AppSettingsManager


       implicit val authSettings = jsonFormat3(AuthSettings);

       val  routes = pathPrefix("config") {
           path("settings"/"auth") {
             post {
               entity(as[AuthSettings]) { authSettings =>
                 val save: Future[AuthSettings] = persistanceMgr.persistAuthSettings(authSettings)
                 onComplete(save) { maybeAuthSettings =>
                   complete("Operation successfull!!")
                 }
               }
             }
           } ~
             put {
               entity(as[AuthSettings]) { authSettings =>
                 val save: Future[AuthSettings] = persistanceMgr.persistAuthSettings(authSettings)
                 onComplete(save) { done =>
                   complete("Operation successfull!!")
                 }
               }
             }~
             get {
               entity(as[List[AuthSettings]]) { result =>
                  val settingsList: Future[List[AuthSettings]] =  persistanceMgr.getSettings()
                 onComplete(settingsList) { done =>
                  complete(settingsList)
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
