package com.activegrid.services

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol._
import com.activegrid.utils.Utils
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.activegrid.entities.Software
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future



class CatalogService {

  var utils: Utils = new Utils();

  implicit val software = jsonFormat3(Software)


  def routes = pathPrefix("catalog") {
    pathEnd {
      post {
        entity(as[Software]) { software =>
          val save: Future[Software] = Future{
            software
          }
          onComplete(save) { done =>
            complete("Operation successfull")
          }
        }
      }

    }
  }
}