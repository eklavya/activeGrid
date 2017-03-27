
package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.util.Try

/**
  * Created by sivag on 17/1/17.
  */
class AnsiblePlayBookRunner(workflowContext: WorkflowContext) {

  val logger = Logger(LoggerFactory.getLogger(AnsiblePlayBookRunner.this.getClass.getName))

  def executePlayBook() : Try[Boolean] = {
    //todo implementation.
    Try(false) //dummy response.
  }
  def execute() : Try[Boolean] = {
     executePlayBook() recover {
       case _ => //todo update distributed cache
       logger.error("An unexpected error has occurred")
     }
    Try(true);
  }

  def getWorkflowContext(): WorkflowContext = workflowContext

}