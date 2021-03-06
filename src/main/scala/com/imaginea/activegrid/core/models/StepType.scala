package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
sealed trait StepType {
  val stepType: String
}

object StepType {

  case object INPUT extends StepType {
    override val stepType: String = "INPUT"
  }

  case object EXECUTION extends StepType {
    override val stepType: String = "EXECUTION"
  }

  case object TRIGGER extends StepType {
    override val stepType: String = "TRIGGER"
  }

  def toStepType(stepType: String): StepType = {
    stepType match {
      case "INPUT" => INPUT
      case "EXECUTION" => EXECUTION
      case "TRIGGER" => TRIGGER
    }
  }

  def values: List[StepType] = List(INPUT, EXECUTION, TRIGGER)
}