package com.imaginea.activegrid.core.resources

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.imaginea.activegrid.core.models.ImageInfo
import com.imaginea.activegrid.core.services.CatalogService

/**
  * Created by babjik on 22/9/16.
  */
trait CatalogResource extends MyResource{

  implicit val catalogService: CatalogService


  def catalogRoute: Route = pathPrefix("catalog") {
    path("images") {
      get {
          complete(catalogService.getImages)
      } ~ post {
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
}
