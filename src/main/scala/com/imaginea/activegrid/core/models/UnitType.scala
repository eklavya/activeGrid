package com.imaginea.activegrid.core.models

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

/**
  * Created by sivag on 22/11/16.
  */

sealed trait UnitType {
  def unitType: String

  override def toString: String = unitType
}

case object PERCENTAGE extends UnitType {
  override def unitType: String = "PERCENTAGE"
}

case object BYTES extends UnitType {
  override def unitType: String = "BYTES"
}

case object KILOBYTES extends UnitType {
  override def unitType: String = "KILO_BYTES"
}

case object MEGABYTES extends UnitType {
  override def unitType: String = "MEGA_BYTES"
}

case object GIGABYTES extends UnitType {
  override def unitType: String = "GIGA_BYTES"
}

case object SECONDS extends UnitType {
  override def unitType: String = "SECONDS"
}

case object MILLISECONDS extends UnitType {
  override def unitType: String = "MILLI_SECONDS"
}

case object MICROSECONDS extends UnitType {
  override def unitType: String = "MICRO_SECONDS"
}

case object COUNT extends UnitType {
  override def unitType: String = "COUNT"
}


case object UnitType {
  def toUnitType(metricType: String): UnitType = {
    metricType match {
      case "PERCENTAGE" => PERCENTAGE
      case "BYTES" => BYTES
      case "KILO_BYTES" => KILOBYTES
      case "MEGA_BYTES" => MEGABYTES
      case "GIGA_BYTES" => GIGABYTES
      case "SECONDS" => SECONDS
      case "MILLI_SECONDS" => MILLISECONDS
      case "MICRO_SECONDS" => MICROSECONDS
      case "COUNT" => COUNT
    }
  }
}
object UnitTypeJson extends RootJsonFormat[UnitType] {

  override def write(obj: UnitType): JsValue = {
    JsString(obj.unitType.toString)
  }

  override def read(json: JsValue): UnitType = {
    json match {
      case JsString(str) => UnitType.toUnitType(str)
      case _ => throw DeserializationException("Unable to deserialize Filter Type")
    }
  }
}

