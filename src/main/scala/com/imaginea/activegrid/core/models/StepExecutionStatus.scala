package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
sealed trait StepExecutionStatus {
  val stepExecutionStatus: String

  override def toString: String = super.toString
}

case object StepExecutionStatus {

  case object NOT_STARTED extends StepExecutionStatus {
    override val stepExecutionStatus: String = "Not Started"
  }

  case object STARTED extends StepExecutionStatus {
    override val stepExecutionStatus: String = "Started"
  }

  case object CHANGED extends StepExecutionStatus {
    override val stepExecutionStatus: String = "changed"
  }

  case object FAILED extends StepExecutionStatus {
    override val stepExecutionStatus: String = "failed"
  }

  case object UNCHANGED extends StepExecutionStatus {
    override val stepExecutionStatus: String = "unchanged"
  }

  case object IN_PROGRESS extends StepExecutionStatus {
    override val stepExecutionStatus: String = "In Progress"
  }

  case object UNKNOWN extends StepExecutionStatus {
    override val stepExecutionStatus: String = "Unknown"
  }

  def toStepExecutionStatus(stepExecutionStatus: String): StepExecutionStatus = {
    stepExecutionStatus match {
      case "Not Started" => NOT_STARTED
      case "Started" => STARTED
      case "changed" => CHANGED
      case "failed" => FAILED
      case "unchanged" => UNCHANGED
      case "In Progress" => IN_PROGRESS
      case "Unknown" => UNKNOWN
    }
  }
}
