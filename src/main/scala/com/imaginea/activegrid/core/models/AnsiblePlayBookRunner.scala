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
    //scalastyle:off
    val workflow = workflowContext.workflow
    val execution = workflow.execution.get
    val inventory = execution.inventory.get
    val playBook = workflow.playBooks.head
    val engine = new AnsibleScriptEngine(inventory,workflowContext,playBook)
    engine.executeScript()
  }
  def execute() : Try[Boolean] = {
     executePlayBook() recover {
       case e : Exception =>
         val workflowId = WorkflowContext.get().workflow.id
       logger.error(s"An unexpected error, Execution of workflow ($workflowId) aborted., Removig workflow from running list",e)
       CurrentRunningWorkflows.remove(workflowId.getOrElse(0L));
     }
    Try(true);
  }
  def getWorkflowContext(): WorkflowContext = workflowContext
}