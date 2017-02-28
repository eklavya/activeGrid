package com.imaginea.activegrid.core.models

import akka.actor.{Actor, ActorLogging}
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

/**
  * Created by nagulmeeras on 28/02/17.
  */
case class Message(str: String)

class RequestRouter extends Actor with ActorLogging {
  val hadlerRouter = context.actorOf(FromConfig.props(), "balanceRouter")
  implicit val timeout = Timeout(5 seconds)

  override def receive: Receive = {
    case appsettings: ApplicationSettings =>
      log.info("recieved into router" + appsettings)
      val result = hadlerRouter ? appsettings
      sender() ! result
    case _ =>
      log.info("Unknown message !")
  }
}
