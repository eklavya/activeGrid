package com.imaginea.activegrid.core.models
import scala.util.Try
/**
  * Created by sivag on 17/1/17.
  */
  trait WorkflowProcessor {
  /**
    * @param workflow
    * Halts execution of scrips during execution
    */
  def stopWorkflow(workflow: Workflow): Unit
  /**
    * Workflow either be puppet or ansible service. It delegatge control to respective processor.
    * @param workflowContext
    */
  def executeWorkflow(workflowContext: WorkflowContext, async: Boolean): Unit
}
