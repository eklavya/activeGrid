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
    implicit val softwareFormat = jsonFormat(Software.apply,"id","version","name","provider","downloadURL","port","processNames","discoverApplications")
    implicit val softwarePageFormat = jsonFormat4(Page[Software])
    implicit val ImageFormat = jsonFormat(ImageInfo.apply, "id","state","ownerId","publicValue","architecture","imageType","platform","imageOwnerAlias","name","description","rootDeviceType","rootDeviceName","version")
    implicit val PageFormat = jsonFormat4(Page[ImageInfo])

    var catalogService = new CatalogService()
    val catalogRoutes = pathPrefix("catalog") {
      path("images" / "view") {
        get {
          val getImages = catalogService.getImages
          onComplete(getImages) {
            case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case util.Failure(exception) => complete(StatusCodes.BadRequest, "Unable to Retrieve ImageInfo List. Failed with " + exception)
          }
        }
      } ~ path("images") {
        put {
          entity(as[ImageInfo]) { image =>
            val buildImage = catalogService.buildImage(image)
            onComplete(buildImage) {
              case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
              case util.Failure(exception) => complete(StatusCodes.BadRequest, "Unable to Save Image. Failed with" + exception )
            }
          }
        }
      } ~ path("images" / LongNumber) { imageId =>
        delete {
          val deleteImages = catalogService.deleteImage(imageId)
          onComplete(deleteImages) {
            case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case util.Failure(exception) => complete(StatusCodes.BadRequest, "Unable to Delete Image. Failed with" + exception )
          }
        }
      } ~ path("softwares") {
        put {
          entity(as[Software]) { software =>
            val buildSoftware = catalogService.buildSoftware(software)
            onComplete(buildSoftware) {
              case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
              case util.Failure(exception) => complete(StatusCodes.BadRequest, "Unable to Save Software. Failed with" + exception)
            }
          }
        }
      } ~ path("softwares" / LongNumber) { softwareid =>
        delete {
          val deleteSoftware = catalogService.deleteSoftware(softwareid)
          onComplete(deleteSoftware) {
            case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case util.Failure(exception) => complete(StatusCodes.BadRequest, "Unable to Delete Software. Failed with" + exception )
          }
        }
      } ~ path("softwares") {
        get {
          val getSoftwares = catalogService.getSoftwares
          onComplete(getSoftwares) {
            case util.Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case util.Failure(exception) => complete(StatusCodes.BadRequest, "Unable to Retrieve Softwares List. Failed with " + exception)
          }
        }
      }
    }
    val route = catalogRoutes

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info(s"Server online at http://localhost:9000")
  }
}