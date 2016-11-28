package com.imaginea.activegrid.core.models

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

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

object ConditionTypeJson extends RootJsonFormat[ConditionType] {

  override def write(obj: ConditionType): JsValue = {
    JsString(obj.conditionType.toString)
  }

  override def read(json: JsValue): ConditionType = {
    json match {
      case JsString(str) => ConditionType.toconditionType(str)
      case _ => throw DeserializationException("Unable to deserialize Filter Type")
    }
  }
}
