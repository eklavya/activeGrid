package com.activegrid

import akka.Done
import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.activegrid.model.{ImageInfo, Page}
import com.activegrid.services.CatalogService
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import scala.util._

object Main {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  /*
  Scala futures need an execution context to run on.
  It works exactly like a runnable queue where futures are enqueued and the execution threads (in the thread pool)
  dequeue them and execute.

  Implicit values are hidden arguments.
  Let's say you have a common parameter that needs to be an argument to varioud functions.
  It could be part of config or infrastructure. Instead of passing it to each and every function.
  We can make it implicit and all functions which expect an implicit argument will get it from the local scope.

  read more about futures and implicits.
   */
  implicit val executionContext = system.dispatcher

  // domain model
  final case class Item(name: String, id: Long)

  final case class Order(items: List[Item])
  // formats for unmarshalling and marshalling

  /*
  Here we are creating json codec automatically.
   */
  implicit val itemFormat = jsonFormat2(Item)

  implicit val orderFormat = jsonFormat1(Order)
  // (fake) async database query api
  def fetchItem(itemId: Long): Future[Option[Item]] = Future(Some(Item("item", itemId)))

  def saveOrder(order: Order): Future[Done] = Future(Done)

  def main(args: Array[String]) {

    val itemRoute = get {
      pathPrefix("item" / LongNumber) { id =>

        val maybeItem: Future[Option[Item]] = fetchItem(id)

        onSuccess(maybeItem) {
          case Some(item) => complete(item)
          case None       => complete(StatusCodes.NotFound)
        }
      }
    }

    val orderRoute = post {
      path("create-order") {
        entity(as[Order]) { order =>
          val saved: Future[Done] = saveOrder(order)
          onComplete(saved) { done =>
            complete("order created")
          }
        }
      }
    }

    implicit val ImageFormat = jsonFormat13(ImageInfo)
    implicit val PageFormat = jsonFormat4(Page[ImageInfo])

    var catalogService = new CatalogService()

    val catalogRoutes  = pathPrefix("catalog") {

      path("images"/"view") {
        get {
          val listOfImages : Future[Option[List[ImageInfo]]] = catalogService.getImages()

          onSuccess(listOfImages) {
             case Some(lists) => {
               val startIndex = 0
               val count = lists.size
               val totalObjects = count
               complete(Page[ImageInfo](startIndex,count,totalObjects,lists))
             }
             case None => complete("List not found")
           }
        }
      } ~ path("images") {
        put { entity(as[ImageInfo]) { image =>
          complete(catalogService.buildImage(image))
        }
        }
      } ~ path("images"/Segment){ imageId =>
        delete {
          complete(catalogService.deleteImage(imageId))
        }
        }

    }




    val route = itemRoute ~ orderRoute ~ catalogRoutes

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info(s"Server online at http://localhost:9000")
  }
}