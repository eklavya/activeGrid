package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 28/10/16.
  */
sealed trait ConditionType {
  def conditionType: String

  override def toString: String = conditionType
}


case object GREATER_THAN extends ConditionType {
  override def conditionType: String = "GREATER_THAN"
}

case object LESS_THAN extends ConditionType {
  override def conditionType: String = "LESS_THAN"
}

case object EQUAL_TO extends ConditionType {
  override def conditionType: String = "EQUAL_TO"
}

case object ConditionType {
  def toconditionType(conditionType: String): ConditionType = {
    conditionType match {
      case "GREATER_THAN" => GREATER_THAN
      case "LESS_THAN" => LESS_THAN
      case "EQUAL_TO" => EQUAL_TO
    }
  }
}
