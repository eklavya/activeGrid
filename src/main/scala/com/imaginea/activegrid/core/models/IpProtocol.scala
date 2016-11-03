package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 27/10/16.
  */
sealed trait IpProtocol {
  def value: String
}

case object IpProtocol {

  case object TCP extends IpProtocol {
    override def value: String = "tcp"
  }

  case object UDP extends IpProtocol {
    override def value: String = "udp"
  }

  case object ICMP extends IpProtocol {
    override def value: String = "icmp"
  }

  case object ALL extends IpProtocol {
    override def value: String = "all"
  }

  case object UNRECOGNIZED extends IpProtocol {
    override def value: String = "unrecognized"
  }

  def toProtocol(value: String): IpProtocol = {
    value match {
      case "tcp" => TCP
      case "udp" => UDP
      case "icmp" => ICMP
      case "all" => ALL
      case _ => UNRECOGNIZED
    }
  }
}
