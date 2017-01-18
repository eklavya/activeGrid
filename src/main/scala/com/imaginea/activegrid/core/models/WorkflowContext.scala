package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 11/1/17.
  */
case class WorkflowContext(workflow: Workflow,
                           workflowListener: WorkflowListener,
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

