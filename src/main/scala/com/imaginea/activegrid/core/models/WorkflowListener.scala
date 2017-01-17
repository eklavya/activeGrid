package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 17/1/17.
  */
trait WorkflowListener {
  def workflowStarted(we: WorkflowEvent)

  def workflowCompleted(we: WorkflowEvent)

  def workflowFailed(we: WorkflowEvent)

  def workflowStepFailed(we: WorkflowEvent)
}
