package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.discovery.models.Site

import scala.collection.immutable.HashMap

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class APMServerDetails(name: String,
                            serverUrl: String,
                            val headers : Map[String,String],
                            monitoredSite: Site,
                            provider: APMProvider) extends BaseEntity{

}

class APMProvider extends Enumeration {
  type APMProvider = Value
  val GRAPHITE, NEWRELIC, APPDYNAMICS = Value
}
