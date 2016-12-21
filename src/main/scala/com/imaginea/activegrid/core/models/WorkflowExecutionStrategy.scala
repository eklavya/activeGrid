package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 20/12/16.
  */
sealed trait WorkflowExecutionStrategy {
  val executionStrategy: String
}

case object WorkflowExecutionStrategy {

  case object STOP_ON_STEP_FAILURE extends WorkflowExecutionStrategy {
    override val executionStrategy: String = "STOP_ON_STEP_FAILURE"
  }

  case object CONTINUE_ON_STEP_FAILURE extends WorkflowExecutionStrategy {
    override val executionStrategy: String = "CONTINUE_ON_STEP_FAILURE"
  }

  def toExecutionStrategy(strategyName: String): WorkflowExecutionStrategy = {
    strategyName match {
      case "STOP_ON_STEP_FAILURE" => STOP_ON_STEP_FAILURE
      case "CONTINUE_ON_STEP_FAILURE" => CONTINUE_ON_STEP_FAILURE
    }
  }
}
