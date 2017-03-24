package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 04/01/17.
  */
case class WorkflowContext(workflow: Workflow,
                           workflowListener: WorkflowExecutionListener,
                           workflowExecLogListener: WorkflowExecLogListener,
                           currentStep: Option[Step],
                           stepContextMap: Option[Map[Step, StepExecutonContext]],
                           workFlowExecutionStatus: Option[WorkFlowExecutionStatus]
                          )

object WorkflowContext {
  def get() : WorkflowContext = {
    AnyRef.asInstanceOf[WorkflowContext] // TODO by Shiva
  }
}
