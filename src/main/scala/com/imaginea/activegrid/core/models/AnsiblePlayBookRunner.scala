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
       case e : Exception =>
         val workflowId = WorkflowContext.get().workflow.id
       logger.error("An unexpected error, Execution of workflow "+workflowId+" aborted., Removig workflow from running list",e)
       CurrentRunningWorkflows.remove(workflowId.getOrElse(0L));
     }
    Try(true);
  }
  def getWorkflowContext(): WorkflowContext = workflowContext
}