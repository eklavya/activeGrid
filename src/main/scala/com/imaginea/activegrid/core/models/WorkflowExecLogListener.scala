package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 11/1/17.
  */
class WorkflowExecLogListener
{
   def newLine(line:String,execution:WorkflowExecution): Unit = {
    val logs = execution.logs + line
    val updateVals = Map("logs" -> logs)
    Neo4jRepository.updateNodeByLabelAndId[WorkflowExecution](
      WorkflowExecution.labelName,execution.id.getOrElse(0L),updateVals)
  }
}
object WorkflowExecLogListener {
  def get() : WorkflowExecLogListener = {
    new WorkflowExecLogListener()
  }
}
