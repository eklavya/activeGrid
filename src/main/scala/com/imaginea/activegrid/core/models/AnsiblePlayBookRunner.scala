package com.imaginea.activegrid.core.models

import akka.actor.Actor
import akka.actor.Actor.Receive
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 17/1/17.
  */
class AnsiblePlayBookRunner(workflowContext: WorkflowContext) extends Actor {

   def getWorkflowContext() = workflowContext
  val logger = Logger(LoggerFactory.getLogger(AnsiblePlayBookRunner.this.getClass.getName))

  override def receive: Receive = {
    //to implemenation
    case "runScript" =>
      //todo implementation
      logger.info("received runscript")
    case _ => logger.info("received unknown message")
  }
}
