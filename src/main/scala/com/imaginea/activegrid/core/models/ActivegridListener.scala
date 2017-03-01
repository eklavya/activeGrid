package com.imaginea.activegrid.core.models

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 28/2/17.
  */
class ActivegridListener extends Actor{

  //def apply(asPort: Int, appPort: Int): ActivegridListener = new ActivegridListener(asPort, appPort)

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))


  override def receive: Receive = {
    case "Welcome" => logger.info("Message received....."+ self.path)
  }

}
