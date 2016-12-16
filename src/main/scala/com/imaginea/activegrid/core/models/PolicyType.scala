package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 15/12/16.
  */
trait PolicyType {
 val plcyType:String
  override def toString : String = plcyType
}
object TAG extends PolicyType {
 override val plcyType = "TAG"
}
object RSRVD_INSTANCE extends PolicyType {
 override val plcyType = "RESERVED_INSTANCE"
}
object SCALING extends PolicyType {
 override val plcyType = "SCALING"
}
object PolicyType {
  def toType(plcyType:String) : PolicyType = {
    plcyType match {
      case "TAG" => TAG
      case "RESERVED_INSTANCE" => RSRVD_INSTANCE
      case "SCALING"  => SCALING
      case _ => TAG
    }
  }
}