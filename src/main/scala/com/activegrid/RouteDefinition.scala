package com.activegrid



import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Flow
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future



class RouteDefinition {

  var utils: Utils = new Utils();

  implicit val software = jsonFormat3(Software)

  def catalogRoute: Route = pathPrefix("catalog") {
    pathEnd {
      post {
        entity(as[Software]) { software =>
          val save: Future[Software] = utils.persistSoftwareDetails(software);
          onComplete(save) { done =>
            complete("Operation successfull")
          }
        }
      }

    }
  }
}