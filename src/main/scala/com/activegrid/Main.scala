package com.activegrid

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.activegrid.model.{ImageInfo, Page, Software}
import com.activegrid.services.CatalogService
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

object Main {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def main(args: Array[String]) {
    implicit val softwareFormat = jsonFormat8(Software)
    implicit val softwarePageFormat = jsonFormat4(Page[Software])
    implicit val ImageFormat = jsonFormat13(ImageInfo)
    implicit val PageFormat = jsonFormat4(Page[ImageInfo])

    var catalogService = new CatalogService()
    val catalogRoutes = pathPrefix("catalog") {
      path("images" / "view") {
        get {
          val getImages = catalogService.getImages
          onSuccess(getImages) {
            case Some(successResponse) => complete(StatusCodes.OK, successResponse)
            case None => complete(StatusCodes.BadRequest, "Unable to Retrieve Images")
          }
        }
      } ~ path("images") {
        put {
          entity(as[ImageInfo]) { image =>
            val buildImage = catalogService.buildImage(image)
            onSuccess(buildImage) {
              case Some(successResponse) => complete(StatusCodes.OK, successResponse)
              case None => complete(StatusCodes.BadRequest, "Unable to Save Image")
            }
          }
        }
      } ~ path("images" / LongNumber) { imageId =>
        delete {
          val deleteImages = catalogService.deleteImage(imageId)
          onSuccess(deleteImages) {
            case Some(successResponse) => complete(StatusCodes.OK, successResponse)
            case None => complete(StatusCodes.BadRequest, "Unable to Delete Image")
          }
          complete(catalogService.deleteImage(imageId))
        }
      } ~ path("softwares") {
        put {
          entity(as[Software]) { software =>
            val buildSoftware = catalogService.buildSoftware(software)
            onSuccess(buildSoftware) {
              case Some(a) => complete(StatusCodes.OK, a)
              case None => complete(StatusCodes.BadRequest, "Unable to Save Software")
            }
          }
        }
      } ~ path("softwares" / LongNumber) { softwareid =>
        delete {
          val deleteSoftware = catalogService.deleteImage(softwareid)
          onSuccess(deleteSoftware) {
            case Some(successResponse) => complete(StatusCodes.OK, successResponse)
            case None => complete(StatusCodes.BadRequest, "Unable to Delete Software")
          }
        }
      } ~ path("softwares") {
        get {
          val getSoftwares = catalogService.getSoftwares
          onSuccess(getSoftwares) {
            case Some(successResponse) => complete(StatusCodes.OK, successResponse)
            case None => complete(StatusCodes.BadRequest, "Unable to Retrieve Softwares")
          }
        }
      }
    }
    val route = catalogRoutes

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info(s"Server online at http://localhost:9000")
  }
}