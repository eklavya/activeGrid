package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 21/12/16.
  */
sealed trait CumulativeStepExecutionStatus {
  val cumulativeStepExecutionStatus: String

  override def toString: String = super.toString
}

case object CumulativeStepExecutionStatus {

  case object NOT_STARTED extends CumulativeStepExecutionStatus {
    override val cumulativeStepExecutionStatus: String = "Not Started"
  }

  case object SUCCESS extends CumulativeStepExecutionStatus {
    override val cumulativeStepExecutionStatus: String = "success"
  }

  case object FAILURE extends CumulativeStepExecutionStatus {
    override val cumulativeStepExecutionStatus: String = "failure"
  }

  case object IN_PROGRESS extends CumulativeStepExecutionStatus {
    override val cumulativeStepExecutionStatus: String = "In Progress"
  }

  def toCumulativeStepExecutionStatus(cumulativeStepExecutionStatus: String): CumulativeStepExecutionStatus = {
    cumulativeStepExecutionStatus match {
      case "Not Started" => NOT_STARTED
      case "success" => SUCCESS
      case "failure" => FAILURE
      case "In Progress" => IN_PROGRESS
    }
  }
}

