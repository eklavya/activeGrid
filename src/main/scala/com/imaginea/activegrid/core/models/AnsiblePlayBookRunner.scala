package com.imaginea.activegrid.core.models

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 17/1/17.
  */
class AnsiblePlayBookRunner(workflowContext: WorkflowContext) extends Actor with Runnable {

  def getWorkflowContext() : WorkflowContext = workflowContext

  val logger = Logger(LoggerFactory.getLogger(AnsiblePlayBookRunner.this.getClass.getName))

  override def receive: Receive = {
    //to implemenation
    case "runScript" =>
      //todo implementation
      logger.info("received runscript")
    case _ => logger.info("received unknown message")
  }

  //todo implementation
  override def run(): Unit = ???
}
