package com.activegrid

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.activegrid.model.{Page, Software, Test}
import com.activegrid.services.CatalogService
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future

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
          case None => complete(StatusCodes.NotFound)
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

    implicit val softwareFormat = jsonFormat12(Software)
    implicit val softwarePageFormat = jsonFormat4(Page[Software])
    implicit val testList = jsonFormat1(Test)

    var catalogService = new CatalogService()

    val catalogRoutes = pathPrefix("catalog") {

      path("softwares") {
        put {
          entity(as[Software]) { software =>
            catalogService.buildSoftware(software)
            complete("success")
          }
        }
      } ~ path("softwares" / Segment) { softwareid =>
        delete {
          complete(catalogService.deleteSoftware(softwareid))
        }
      } ~ path("softwares") {
        get {
          val listOfSoftwares: Future[Option[List[Software]]] = catalogService.getSoftwares()

          onSuccess(listOfSoftwares) {
            case Some(lists) => {
              complete(new Page[Software](lists))
            }
            case None => complete("List not found")
          }
        }
      } ~ path("test") {
        put {
          entity(as[Test]) { testValue =>
            catalogService.listTestPut(testValue)
            complete("success")

          }
        }
      }
    }
    val route = itemRoute ~ orderRoute ~ catalogRoutes

    val bindingFuture = Http().bindAndHandle(route, "localhost", 9000)
    logger.info(s"Server online at http://localhost:9000")
  }
}