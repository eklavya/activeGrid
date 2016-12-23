package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 20/12/16.
  */
sealed trait WorkFlowExecutionStatus {
  val executionStatus: String
}

object WorkFlowExecutionStatus {

  case object NOT_STARTED extends WorkFlowExecutionStatus {
    override val executionStatus: String = "Not Started"
  }

  case object STARTED extends WorkFlowExecutionStatus {
    override val executionStatus: String = "Started"
  }

  case object SUCCESS extends WorkFlowExecutionStatus {
    override val executionStatus: String = "success"
  }

  case object FAILED extends WorkFlowExecutionStatus {
    override val executionStatus: String = "Failed"
  }

  case object INTERRUPTED extends WorkFlowExecutionStatus {
    override val executionStatus: String = "Interrupted"
  }

  case object STEP_FAILED extends WorkFlowExecutionStatus {
    override val executionStatus: String = "Step Failed"
  }

  def toExecutionStatus(status: String): WorkFlowExecutionStatus = {
    status match {
      case "Not Started" => NOT_STARTED
      case "Started" => STARTED
      case "success" => SUCCESS
      case "Failed" => FAILED
      case "Interrupted" => INTERRUPTED
      case "Step Failed" => STEP_FAILED
    }
  }
}
