package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 25/10/16.
  */
sealed trait InstanceProvider {
  def instanceProvider: String

  override def toString: String = instanceProvider
}

case object AWS extends InstanceProvider {
  override def instanceProvider: String = "AWS"
}

case object OPENSTACK extends InstanceProvider {
  override def instanceProvider: String = "OPENSTACK"
}

case object LAN extends InstanceProvider {
  override def instanceProvider: String = "LAN"
}

case object InstanceProvider {
  def toInstanceProvider(instanceProvider: String): InstanceProvider = {
    instanceProvider match {
      case "AWS" => AWS
      case "OPENSTACK" => OPENSTACK
      case "LAN" => LAN
    }
  }
}