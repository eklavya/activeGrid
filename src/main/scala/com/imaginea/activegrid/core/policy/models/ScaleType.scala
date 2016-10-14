package com.imaginea.activegrid.core.policy.models

/**
 * Created by ranjithrajd on 12/10/16.
 */
sealed trait ScaleType {
  def name: String
  override def toString: String = name
}

case object ScaleUp extends ScaleType { val name = "SCALE_UP" }
case object ScaleDown extends ScaleType { val name = "SCALE_DOWN" }

object ScaleType {
  implicit def toScaleType(name: String): ScaleType = name match {
    case "SCALE_UP" => ScaleUp
    case "SCALE_DOWN" => ScaleDown
  }
}
