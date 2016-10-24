package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 14/10/16.
  */
sealed trait APMProvider {
  def provider: String

  override def toString: String = provider
}

case object GRAPHITE extends APMProvider {
  val provider = "GRAPHITE"
}

case object NEWRELIC extends APMProvider {
  val provider = "NEWRELIC"
}

case object APPDYNAMICS extends APMProvider {
  val provider = "APPDYNAMICS"
}

case object APMProvider {
  def toProvider(provider: String): APMProvider = {
    provider match {
      case "GRAPHITE" => GRAPHITE
      case "NEWRELIC" => NEWRELIC
      case "APPDYNAMICS" => APPDYNAMICS
    }
  }
}