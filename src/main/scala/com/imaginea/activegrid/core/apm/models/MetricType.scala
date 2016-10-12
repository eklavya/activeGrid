package com.imaginea.activegrid.core.apm.models

/**
 * Created by ranjithrajd on 12/10/16.
 */

class MetricType extends Enumeration {
  type MetricType = Value
  val CPU_UTILIZATION, DISK_UTILIZATION, MEMORY_UTILIZATION, RESPONSE_TIME, CALLS_PER_MINUTE, IO_READS, IO_WRITES = Value
}

class UnitType extends Enumeration {
  type UnitType = Value
  val PERCENTAGE, BYTES, KILO_BYTES, MEGA_BYTES, GIGA_BYTES, SECONDS, MILLI_SECONDS, MICRO_SECONDS, COUNT = Value
}
