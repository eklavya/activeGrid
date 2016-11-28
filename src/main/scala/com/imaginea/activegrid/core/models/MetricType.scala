package com.imaginea.activegrid.core.models

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

/**
  * Created by sivag on 22/11/16.
  */
sealed trait MetricType {
  def metricType: String

  override def toString: String = metricType
}

case object CPU extends MetricType {
  override def metricType: String = "CPU_UTILIZATION"
}

case object DISK extends MetricType {
  override def metricType: String = "DISK_UTILIZATION"
}

case object MEMORY extends MetricType {
  override def metricType: String = "MEMORY_UTILIZATION"
}


case object RESPONSE extends MetricType {
  override def metricType: String = "RESPONSE_TIME"
}

case object CALLSPERMINUTE extends MetricType {
  override def metricType: String = "CALLS_PER_MINUTE"
}

case object IOREADS extends MetricType {
  override def metricType: String = "IO_READS"
}

case object IOWRITES extends MetricType {
  override def metricType: String = "IO_WRITES"
}

case object MetricType {
  def toMetricType(metricType: String): MetricType = {
    metricType match {
      case "CPU_UTILIZATION" => CPU
      case "DISK_UTILIZATION" => DISK
      case "MEMORY_UTILIZATION" => MEMORY
      case "RESPONSE_TIME" => RESPONSE
      case "CALLS_PER_MINUTE" => CALLSPERMINUTE
      case "IO_READS" => IOREADS
      case "IO_WRITES" => IOWRITES
    }
  }
}


object MetricTypeFormat extends RootJsonFormat[MetricType] {

  override def write(obj: MetricType): JsValue = {
    JsString(obj.metricType.toString)
  }

  override def read(json: JsValue): MetricType = {
    json match {
      case JsString(str) => MetricType.toMetricType(str)
      case _ => throw DeserializationException("Unable to deserialize Filter Type")
    }
  }
}
