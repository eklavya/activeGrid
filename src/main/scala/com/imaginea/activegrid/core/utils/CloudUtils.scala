package com.imaginea.activegrid.core.utils


import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.Complete
import akka.parboiled2.RuleTrace.Fail
import akka.stream.ActorMaterializer
import com.imaginea.Main._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}


/**
  * Created by sivag on 28/2/17.
  */
object CloudUtils {

  val ports : Map[Int,Int] = Map(5000->6000,5001->6001,5003->6003)

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def startAppCloud(route: Route): Unit ={

      for ((acPort,appPort) <- ports) {
        val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + acPort).
          withFallback(ConfigFactory.load("activegrid.conf"))
        // Create an Akka system
        logger.info("Starting application on port "+appPort)
        implicit val system = ActorSystem("ActorSystem-"+acPort,config)
        implicit val materializer = ActorMaterializer()
        implicit val executionContext = system.dispatcher
        Http().bindAndHandle(route,"localhost",appPort)
        val lister = system.actorOf(Props[ActivegridListener], name = "clusterListener")
        lister ! "Welcome"
      }

  }
}
