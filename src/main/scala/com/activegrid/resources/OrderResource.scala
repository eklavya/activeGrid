package com.activegrid.resources

import scala.concurrent.{ExecutionContext, Future}

import akka.http.scaladsl.server.Directives._
import akka.Done

import com.activegrid.services.OrderService
import com.activegrid.models.{OrderJsonProtocol, Order}

/**
 * Order rest service. <code>
 * curl http://localhost:9000/create-order -X POST -H 'Content-Type: application/json' --data-binary '{"items":[{"name":"item","id":1002},{"name":"item","id":1002}]}'
 * </code>
 */

class OrderResource(implicit executionContext: ExecutionContext){

  // Json Marshalling / Unmarshalling for response object
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import OrderJsonProtocol._

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

