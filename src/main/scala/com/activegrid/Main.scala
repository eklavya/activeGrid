package com.activegrid

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.activegrid.model.{ImageInfo, Page, Software}
import com.activegrid.services.CatalogService
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

object Main {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val config = ConfigFactory.load

  def main(args: Array[String]) {
    implicit val softwareFormat = jsonFormat8(Software)
    implicit val softwarePageFormat = jsonFormat4(Page[Software])
    implicit val ImageFormat = jsonFormat13(ImageInfo)
    implicit val PageFormat = jsonFormat4(Page[ImageInfo])

    var catalogService = new CatalogService()
    val catalogRoutes = pathPrefix("catalog") {
      path("images" / "view") {
        get {
          complete(catalogService.getImages)
        }
      } ~ path("images") {
        put {
          entity(as[ImageInfo]) { image =>
            complete(catalogService.buildImage(image))
          }
        }
      } ~ path("images" / LongNumber) { imageId =>
        delete {
          complete(catalogService.deleteImage(imageId))
        }
      } ~ path("softwares") {
        put {
          entity(as[Software]) { software =>
            complete(catalogService.buildSoftware(software))
          }
        }
      } ~ path("softwares" / LongNumber) { softwareid =>
        delete {
          complete(catalogService.deleteSoftware(softwareid))
        }
      } ~ path("softwares") {
        get {
          complete(catalogService.getSoftwares)
        }
      }
    }
    val route = catalogRoutes

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info(s"Server online at http://localhost:9000")
  }
}