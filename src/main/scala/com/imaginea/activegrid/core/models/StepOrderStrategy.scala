package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 20/12/16.
  */
sealed trait StepOrderStrategy {
  val orderStrategy: String
}

object StepOrderStrategy {

  case object SEQUENTIAL extends StepOrderStrategy {
    override val orderStrategy: String = "SEQUENTIAL"
  }

  case object PARALLEL extends StepOrderStrategy {
    override val orderStrategy: String = "PARALLEL"
  }

  def toOrderStrategy(strategyName: String): StepOrderStrategy = {
    strategyName match {
      case "SEQUENTIAL" => SEQUENTIAL
      case "PARALLEL" => PARALLEL
    }
  }
}