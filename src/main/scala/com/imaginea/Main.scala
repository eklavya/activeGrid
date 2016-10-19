package com.imaginea

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer


import com.imaginea.activegrid.core.controller.UserServiceController
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Created by babjik on 22/9/16.
 */
object Main extends App {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val config = ConfigFactory.load
  implicit val system = ActorSystem()
  //providing implicit materializer to Bind HTTP with Akka Flow
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val userService: UserServiceController = new UserServiceController

  val route: Route = userService.userRoute

  val bindingFuture = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
  logger.info(s"Server online at http://${config.getString("http.host")}:${config.getInt("http.port")}")

}