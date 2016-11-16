package com.imaginea.activegrid.core.models

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