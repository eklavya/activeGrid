package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 28/10/16.
  */
sealed trait ScaleType {
  def scaleType: String

  override def toString: String = scaleType
}

case object SCALE_UP extends ScaleType {
  override def scaleType: String = "SCALE_UP"
}

case object SCALE_DOWN extends ScaleType {
  override def scaleType: String = "SCALE_DOWN"
}

case object ScaleType {
  def toScaleType(scaleType: String): ScaleType = {
    scaleType match {
      case "SCALE_UP" => SCALE_UP
      case "SCALE_DOWN" => SCALE_DOWN
    }
  }
}