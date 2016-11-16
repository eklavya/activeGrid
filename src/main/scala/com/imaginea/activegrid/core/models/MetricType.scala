package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 28/10/16.
  */
sealed trait MetricType {
  def metricType: String

  override def toString: String = metricType
}

case object CPU_UTILIZATION extends MetricType {
  override def metricType: String = "CPU_UTILIZATION"
}

case object DISK_UTILIZATION extends MetricType {
  override def metricType: String = "DISK_UTILIZATION"
}

case object MEMORY_UTILIZATION extends MetricType {
  override def metricType: String = "MEMORY_UTILIZATION"
}


case object RESPONSE_TIME extends MetricType {
  override def metricType: String = "RESPONSE_TIME"
}

case object CALLS_PER_MINUTE extends MetricType {
  override def metricType: String = "CALLS_PER_MINUTE"
}

case object IO_READS extends MetricType {
  override def metricType: String = "IO_READS"
}

case object IO_WRITES extends MetricType {
  override def metricType: String = "IO_WRITES"
}

case object MetricType {
  def toMetricType(metricType: String): MetricType = {
    metricType match {
      case "CPU_UTILIZATION" => CPU_UTILIZATION
      case "DISK_UTILIZATION" => DISK_UTILIZATION
      case "MEMORY_UTILIZATION" => MEMORY_UTILIZATION
      case "RESPONSE_TIME" => RESPONSE_TIME
      case "CALLS_PER_MINUTE" => CALLS_PER_MINUTE
      case "IO_READS" => IO_READS
      case "IO_WRITES" => IO_WRITES
    }
  }
}