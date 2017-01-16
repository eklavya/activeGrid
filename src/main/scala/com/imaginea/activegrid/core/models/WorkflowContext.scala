package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 04/01/17.
  */
case class WorkflowContext(workflow: Workflow,
                           currentStep: Step,
                           stepContextMap: Map[Step, StepExecutonContext],
                           workFlowExecutionStatus: WorkFlowExecutionStatus
                          )

object WorkflowContext {
  def get() : WorkflowContext = {
    AnyRef.asInstanceOf[WorkflowContext] // TODO by Shiva
  }
}
