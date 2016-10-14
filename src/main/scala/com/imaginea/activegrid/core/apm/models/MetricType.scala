package com.imaginea.activegrid.core.apm.models

/**
 * Created by ranjithrajd on 12/10/16.
 */

sealed trait MetricType{ def name: String }
case object CpuUtilization extends MetricType { val name = "CPU_UTILIZATION" }
case object DiskUtilization extends MetricType { val name = "DISK_UTILIZATION" }
case object MemoryUtilization extends MetricType { val name = "MEMORY_UTILIZATION" }
case object ResponseTime extends MetricType { val name = "RESPONSE_TIME" }
case object CallsPerMinute extends MetricType { val name = "CALLS_PER_MINUTE" }
case object IOReads extends MetricType { val name = "IO_READS" }
case object IOWrites extends MetricType { val name = "IO_WRITES" }


sealed trait UnitType{ def name: String }
case object Percentage extends UnitType { val name = "PERCENTAGE" }
case object Bytes extends UnitType { val name = "BYTES" }
case object KiloBytes extends UnitType { val name = "KILO_BYTES" }
case object MegaBytes extends UnitType { val name = "MEGA_BYTES" }
case object GigaBytes extends UnitType { val name = "GIGA_BYTES" }
case object Seconds extends UnitType { val name = "SECONDS" }
case object MilliSeconds extends UnitType { val name = "MILLI_SECONDS" }
case object MicroSeconds extends UnitType { val name = "MICRO_SECONDS" }
case object Count extends UnitType { val name = "COUNT" }
