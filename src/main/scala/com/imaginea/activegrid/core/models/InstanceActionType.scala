package com.imaginea.activegrid.core.models

/**
 * Created by ranjithrajd on 24/11/16.
 */
trait InstanceActionType {
  def name: String

  override def toString: String = name
}

object InstanceActionType {

  case object Start extends InstanceActionType {
    override def name: String = "start"
  }

  case object Stop extends InstanceActionType {
    override def name: String = "stop"
  }

  case object CreateSnapshot extends InstanceActionType {
    override def name: String = "create-snapshot"
  }

  case object CreateImage extends InstanceActionType {
    override def name: String = "create-image"
  }

  case object NoOpt extends InstanceActionType {
    override def name: String = "NoOpt"
  }

  implicit def toType(name: String): InstanceActionType = name match {
    case "start" => Start
    case "stop" => Stop
    case "create-snapshot" => CreateSnapshot
    case "create-image" => CreateImage
    case _ => NoOpt //None Type
  }
}
