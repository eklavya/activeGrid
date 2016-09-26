package com.activegrid

import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import com.activegrid.resources.{OrderResource, ItemResource}


/**
 * Scala futures need an execution context to run on.
 * It works exactly like a runnable queue where futures are enqueued and the execution threads (in the thread pool)
 * dequeue them and execute.
 *
 * Implicit values are hidden arguments.
 * Let's say you have a common parameter that needs to be an argument to various functions.
 * It could be part of config or infrastructure. Instead of passing it to each and every function.
 * We can make it implicit and all functions which expect an implicit argument will get it from the local scope.
 * read more about futures and implicits.
 */

object Main extends App{

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val config = system.settings.config
  val interface = config.getString("http.interface")
  val port = config.getInt("http.port")

  /* Execution context for Future execution */
  implicit val executionContext = system.dispatcher

  val itemRoute = new ItemResource().route
  val orderRoute = new OrderResource().route
  val route = itemRoute ~ orderRoute

  val bindingFuture = Http().bindAndHandle(route, interface, port)

  // Logging the server binding status
  bindingFuture.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      println(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) ⇒
      println(s"Binding failed with ${e.getMessage}")
  }
}