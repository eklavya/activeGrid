package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

/**
  * Created by sivag on 17/1/17.
  */
class AnsiblePlayBookRunner(workflowContext: WorkflowContext) {

  val logger = Logger(LoggerFactory.getLogger(AnsiblePlayBookRunner.this.getClass.getName))

  def execute(): Unit = {
    val workflow = workflowContext.workflow
    val execution = workflow.execution.get
    if (execution.inventory.isDefined && !workflow.playBooks.isEmpty) {
      val inventory = execution.inventory.get
      val playBook = workflow.playBooks.head
      val engine = new AnsibleScriptEngine(inventory, workflowContext, playBook)
      engine.executeScript() match {
        case Failure(e) =>
          val workflowId = WorkflowContext.get().workflow.id
          logger.error(s"An unexpected error, Execution of workflow ($workflowId) aborted., Removig workflow from running list", e)
          CurrentRunningWorkflows.remove(workflowId.getOrElse(0L));
        case Success(started) =>
          val workflowId = WorkflowContext.get().workflow.id
          if (started) {
            logger.error(s"Workflow ($workflowId) started successfully. Adding workflow to running list")
            CurrentRunningWorkflows.add(workflowId.getOrElse(0L));
          }
          else {
            logger.error(s" Failed to start Workflow ($workflowId), Please try again")
          }
      }
    } else {
      logger.error(s"Failed to start workflow(${workflow.id.getOrElse(0L)})");
    }
  }

  def getWorkflowContext(): WorkflowContext = workflowContext
}