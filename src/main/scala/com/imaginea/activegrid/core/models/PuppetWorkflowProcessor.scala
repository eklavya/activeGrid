package com.imaginea.activegrid.core.models
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import scala.util.Try
/**
  * Created by sivag on 17/1/17.
  */
object PuppetWorkflowProcessor extends WorkflowProcessor {
  val logger = Logger(LoggerFactory.getLogger(PuppetWorkflowProcessor.this.getClass.getName))
  override def stopWorkflow(workflow: Workflow): Unit = {
  }
  /*
   * Workflow either be puppet or ansible service. It delegatge control to respective processor.
   * @param workflowContext
   */
  override def executeWorkflow(workflowContext: WorkflowContext, async: Boolean): Unit = {
    
  }
}
