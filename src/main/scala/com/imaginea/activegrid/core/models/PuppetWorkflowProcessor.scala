package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 17/1/17.
  */
class PuppetWorkflowProcessor extends WorkflowProcessor {

  val logger = Logger(LoggerFactory.getLogger(PuppetWorkflowProcessor.this.getClass.getName))


  def getProcessor() : WorkflowProcessor = {
    new PuppetWorkflowProcessor()
  }

  override def stopWorkflow(workflow: Workflow): Unit = {

  }

  /**
    * Workflow either be puppet or ansible service. It delegatge control to respective processor.
    *
    * @param workflow
    */
  override def executeWorkflow(workflow: Workflow): Unit = ???
}
