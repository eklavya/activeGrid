package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 19/12/16.
  */
sealed trait WorkflowMode {
  val workflowMode: String

  override def toString: String = super.toString
}

case object AGENT extends WorkflowMode {
  override val workflowMode: String = "AGENT"
}

case object AGENT_LESS extends WorkflowMode {
  override val workflowMode: String = "AGENT_LESS"
}

case object WorkflowMode {
  def toWorkFlowMode(workflowMode: String): WorkflowMode = {
    workflowMode match {
      case "AGENT" => AGENT
      case "AGENT_LESS" => AGENT_LESS
    }
  }
}