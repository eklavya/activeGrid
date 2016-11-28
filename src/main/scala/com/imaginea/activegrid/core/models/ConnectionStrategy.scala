package com.imaginea.activegrid.core.models

/**
 * Created by ranjithrajd on 23/11/16.
 */
sealed trait ConnectionStrategy {
  def name: String

  override def toString: String = name
}

object ConnectionStrategy {

  case object Ssh extends ConnectionStrategy {
    val name = "SSH"
  }

  case object SecurityGroup extends ConnectionStrategy {
    val name = "SECURITY_GROUP"
  }

  implicit def toConnectionStrategy(name: String): ConnectionStrategy = name match {
    case "SSH" => Ssh
    case "SECURITY_GROUP" => SecurityGroup
    case _ => Ssh //None value
  }
}