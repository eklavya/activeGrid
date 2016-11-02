package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 28/10/16.
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

case object KILO_BYTES extends UnitType {
  override def unitType: String = "KILO_BYTES"
}

case object MEGA_BYTES extends UnitType {
  override def unitType: String = "MEGA_BYTES"
}

case object GIGA_BYTES extends UnitType {
  override def unitType: String = "GIGA_BYTES"
}

case object SECONDS extends UnitType {
  override def unitType: String = "SECONDS"
}

case object MILLI_SECONDS extends UnitType {
  override def unitType: String = "MILLI_SECONDS"
}

case object MICRO_SECONDS extends UnitType {
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
      case "KILO_BYTES" => KILO_BYTES
      case "MEGA_BYTES" => MEGA_BYTES
      case "GIGA_BYTES" => GIGA_BYTES
      case "SECONDS" => SECONDS
      case "MILLI_SECONDS" => MILLI_SECONDS
      case "MICRO_SECONDS" => MICRO_SECONDS
      case "COUNT" => COUNT
    }
  }
}