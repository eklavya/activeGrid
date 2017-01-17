package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 17/1/17.
  */
trait StepExecutionProcessor {

  def executeNextStep(workflowContext: WorkflowContext)

  def addNextStep(workflow: Workflow, step: Step)
}
