package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 11/1/17.
  */
class WorkflowContext(workflow: Workflow, workflowListener: WorkflowListener,
                      workflowExecLogListener: WorkflowExecLogListener) {
  def getWorkflow(): Workflow = workflow
}

