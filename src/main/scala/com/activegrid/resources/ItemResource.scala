package com.activegrid.resources

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol._

import com.activegrid.services.ItemService
import com.activegrid.models.Item

/**
 * Order rest service. <code>
 * curl http://localhost:9000/item/1002 -X GET
 * </code>
 */

class ItemResource (implicit executionContext: ExecutionContext) {

  // Json Marshalling / Unmarshalling for response object
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // Here we are creating json codec automatically.

  implicit val itemFormat = jsonFormat2(Item)

  val route = get {
    pathPrefix("item" / LongNumber) { id =>
      val maybeItem: Future[Option[Item]] = new ItemService().fetchItem(id)

      onSuccess(maybeItem) {
        case Some(item) => complete(item)
        case None       => complete(StatusCodes.NotFound)
      }
    }
  }
}
