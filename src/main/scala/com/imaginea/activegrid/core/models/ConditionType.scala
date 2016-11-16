package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 28/10/16.
  */
sealed trait ConditionType {
  def conditionType: String

  override def toString: String = conditionType
}


case object GREATERTHAN extends ConditionType {
  override def conditionType: String = "GREATER_THAN"
}

case object LESSTHAN extends ConditionType {
  override def conditionType: String = "LESS_THAN"
}

case object EQUALTO extends ConditionType {
  override def conditionType: String = "EQUAL_TO"
}

case object ConditionType {
  def toconditionType(conditionType: String): ConditionType = {
    conditionType match {
      case "GREATER_THAN" => GREATERTHAN
      case "LESS_THAN" => LESSTHAN
      case "EQUAL_TO" => EQUALTO
    }
  }
}
