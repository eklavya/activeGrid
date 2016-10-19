package com.activegrid

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import com.activegrid.model.{GraphDBExecutor, ImageInfo, Page, Software}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

object Main {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  def main(args: Array[String]) {
    implicit val softwareFormat = jsonFormat(Software.apply, "id", "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
    implicit val softwarePageFormat = jsonFormat4(Page[Software])
    implicit val ImageFormat = jsonFormat(ImageInfo.apply, "id", "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
    implicit val PageFormat = jsonFormat4(Page[ImageInfo])

    def catalogRoutes = pathPrefix("catalog") {
      path("images" / "view") {
        get {
          val getImages: Future[Page[ImageInfo]] = Future {
            val imageLabel: String = "ImagesTest2"
            val nodesList = GraphDBExecutor.getNodesByLabel(imageLabel)
            val imageInfoList = nodesList.map(node => ImageInfo.fromNeo4jGraph(node.getId))

            Page[ImageInfo](0, imageInfoList.size, imageInfoList.size, imageInfoList)
          }

          onComplete(getImages) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to Retrieve ImageInfo List. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Unable to Retrieve ImageInfo List. Failed with : ${exception.getMessage}")
          }
        }
      } ~ path("images") {
        put {
          entity(as[ImageInfo]) { image =>
            val buildImage = Future {
              image.toNeo4jGraph(image)
              "Successfully added ImageInfo"
            }
            onComplete(buildImage) {
              case Success(successResponse) => complete(StatusCodes.OK, successResponse)
              case Failure(exception) =>
                logger.error(s"Unable to Save Image. Failed with : ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, s"Unable to Save Image. Failed with : ${exception.getMessage}")
            }
          }
        }
      } ~ path("images" / LongNumber) { imageId =>
        delete {
          val deleteImages = Future {
            GraphDBExecutor.deleteEntity[ImageInfo](imageId)
            "Successfully deleted ImageInfo"
          }

          onComplete(deleteImages) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to Delete Image. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Unable to Delete Image. Failed with : ${exception.getMessage}")
          }
        }
      } ~ path("softwares") {
        put {
          entity(as[Software]) { software =>
            val buildSoftware = Future {
              software.toNeo4jGraph(software)
              "Saved Software Successfully"
            }
            onComplete(buildSoftware) {
              case Success(successResponse) => complete(StatusCodes.OK, successResponse)
              case Failure(exception) =>
                logger.error(s"Unable to Save Software. Failed with : ${exception.getMessage}", exception)
                complete(StatusCodes.BadRequest, s"Unable to Save Software. Failed with : ${exception.getMessage}")
            }
          }
        }
      } ~ path("softwares" / LongNumber) { softwareId =>
        delete {
          val deleteSoftware = Future {
            GraphDBExecutor.deleteEntity[Software](softwareId)
            "Deleted Successfully"
          }

          onComplete(deleteSoftware) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to Delete Software. Failed with : ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Unable to Delete Software. Failed with : ${exception.getMessage}")
          }
        }
      } ~ path("softwares") {
        get {
          val getSoftwares = Future {
            val softwareLabel: String = "SoftwaresTest2"
            val nodesList = GraphDBExecutor.getNodesByLabel(softwareLabel)
            val softwaresList = nodesList.map(node => Software.fromNeo4jGraph(node.getId))

            Page[Software](0, softwaresList.size, softwaresList.size, softwaresList)
          }
          onComplete(getSoftwares) {
            case Success(successResponse) => complete(StatusCodes.OK, successResponse)
            case Failure(exception) =>
              logger.error(s"Unable to Retrieve Softwares List. Failed with :  ${exception.getMessage}", exception)
              complete(StatusCodes.BadRequest, s"Unable to Retrieve Softwares List. Failed with : ${exception.getMessage}")
          }
        }
      }
    }
    val route = catalogRoutes

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info(s"Server online at http://localhost:9000")
  }


}
