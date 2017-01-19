package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 17/1/17.
  */
class AnsiblePlayBookRunner(workflowContext: WorkflowContext)  {

  def executePlayBook() = ???

  def getWorkflowContext(): WorkflowContext = workflowContext

  val logger = Logger(LoggerFactory.getLogger(AnsiblePlayBookRunner.this.getClass.getName))

}
