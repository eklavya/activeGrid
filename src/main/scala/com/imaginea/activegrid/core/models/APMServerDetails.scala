package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.discovery.models.Site

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class APMServerDetails(name: String,
                            serverUrl: String,
                            val headers: Map[String, String],
                            monitoredSite: Site,
                            provider: APMProvider) extends BaseEntity {

}

sealed trait APMProvider{
  def name: String
  override def toString: String = name
}
case object Graphite extends APMProvider { val name = "GRAPHITE" }
case object NewRelic extends APMProvider { val name = "NEWRELIC" }
case object AppDynamics extends APMProvider { val name = "APPDYNAMICS" }

object APMProvider{
  implicit def toAPMProvider(name: String): APMProvider = name match {
    case "GRAPHITE" => Graphite
    case "NEWRELIC" => NewRelic
    case "APPDYNAMICS" => AppDynamics
  }
}