package com.imaginea.activegrid.core.models

import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

/**
  * Created by sampathr on 28/10/16.
  */
sealed trait ScaleType {
  def scaleType: String

  override def toString: String = scaleType
}

case object SCALEUP extends ScaleType {
  override def scaleType: String = "SCALE_UP"
}

case object SCALEDOWN extends ScaleType {
  override def scaleType: String = "SCALE_DOWN"
}

case object ScaleType {
  def toScaleType(scaleType: String): ScaleType = {
    scaleType match {
      case "SCALE_UP" => SCALEUP
      case "SCALE_DOWN" => SCALEDOWN
    }
  }
}

object ScaleTypeJson extends RootJsonFormat[ScaleType] {

  override def write(obj: ScaleType): JsValue = {
    JsString(obj.scaleType.toString)
  }

  override def read(json: JsValue): ScaleType = {
    json match {
      case JsString(str) => ScaleType.toScaleType(str)
      case _ => throw DeserializationException("Unable to deserialize Filter Type")
    }
  }
}
