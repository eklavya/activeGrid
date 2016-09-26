package com.activegrid.resources

import scala.concurrent.{ExecutionContext, Future}

import com.activegrid.models.{Order, Item}
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.server.Directives._
import akka.Done

import com.activegrid.services.OrderService

/**
 * Order rest service. <code>
 * curl http://localhost:9000/create-order -X POST -H 'Content-Type: application/json' --data-binary '{"items":[{"name":"item","id":1002},{"name":"item","id":1002}]}'
 * </code>
 */

class OrderResource(implicit executionContext: ExecutionContext){

  // Json Marshalling / Unmarshalling for response object
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // Here we are creating json codec automatically
  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)

  val route = post {
    path("create-order") {
      entity(as[Order]) { order =>
        val saved: Future[Done] = new OrderService().saveOrder(order)
        onComplete(saved) { done =>
          complete("order created")
        }
      }
    }
  }
}

