package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 18/1/17.
  */
//implementation of all methods.
class WorkflowEvent{
  //Todo instance variables.
}

class WorkflowExecutionListener {

   def workflowStarted(we: WorkflowEvent): Unit = ??? //todo

   def workflowCompleted(we: WorkflowEvent): Unit = ??? //todo

   def workflowFailed(we: WorkflowEvent): Unit = ??? //todo

   def workflowStepFailed(we: WorkflowEvent): Unit = ??? //todo
}
