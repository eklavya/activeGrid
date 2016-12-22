package com.imaginea.activegrid.core.models

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

/**
  * Created by sivag on 15/12/16.
  */
trait PolicyType {
 val plcyType:String
  override def toString : String = plcyType
  val id:Int = 0
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
object PolicyTypeFormat extends RootJsonFormat[PolicyType] {

  override def write(obj: PolicyType): JsValue = {
    JsString(obj.plcyType.toString)
  }

  override def read(json: JsValue): PolicyType = {
    json match {
      case JsString(str) => PolicyType.toType(str)
      case _ => throw DeserializationException("Unable to deserialize Filter Type")
    }
  }
}