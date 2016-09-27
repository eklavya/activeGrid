package com.imaginea

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.imaginea.activegrid.core.models.ImageInfo
import com.imaginea.activegrid.core.services.CatalogService
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._
/**
  * Created by babjik on 22/9/16.
  */
object Main extends App {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val config = ConfigFactory.load
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val ImageFormat = jsonFormat(ImageInfo, "imageId", "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")

  val catalogService: CatalogService = new CatalogService


  def catalogRoute: Route = pathPrefix("catalog") {
    path("images") {
      get {
        complete(catalogService.getImages)
      } ~ put {
        entity(as[ImageInfo]) { image =>
          complete(catalogService.createImage(image))
        }
      }
    } ~ path("images"/Segment) {id =>
      delete {
        complete(catalogService.deleteImage(id))
      }
    }
  }

  val route: Route = catalogRoute

  val bindingFuture = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
  logger.info(s"Server online at http://${config.getString("http.host")}:${config.getInt("http.port")}")

}
