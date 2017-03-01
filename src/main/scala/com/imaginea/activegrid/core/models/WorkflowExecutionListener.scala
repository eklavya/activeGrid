package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 18/1/17.
  */
//implementation of all methods.
class WorkflowExecutionListener extends WorkflowListener {

  override def workflowStarted(we: WorkflowEvent): Unit = ???

  override def workflowCompleted(we: WorkflowEvent): Unit = ???

  override def workflowFailed(we: WorkflowEvent): Unit = ???

  override def workflowStepFailed(we: WorkflowEvent): Unit = ???
}
