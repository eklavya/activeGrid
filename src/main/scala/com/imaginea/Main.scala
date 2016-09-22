package com.imaginea

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.imaginea.activegrid.core.dao.ImageInfoDAO
import com.imaginea.activegrid.core.resources.CatalogResource
import com.imaginea.activegrid.core.services.CatalogService
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 22/9/16.
  */
object Main extends App with CatalogResource{
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit val config = ConfigFactory.load
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  lazy val catalogService: CatalogService = new CatalogService

  val route: Route = catalogRoute

  val bindingFuture = Http().bindAndHandle(route, config.getString("http.host"), config.getInt("http.port"))
  logger.info(s"Server online at http://${config.getString("http.host")}:${config.getInt("http.port")}")

}
