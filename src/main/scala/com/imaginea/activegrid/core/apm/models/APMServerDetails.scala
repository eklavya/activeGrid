package com.imaginea.activegrid.core.apm.models

import com.imaginea.activegrid.core.discovery.models.Site
import com.imaginea.activegrid.core.models.BaseEntity

import scala.collection.immutable.HashMap

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class APMServerDetails(name: String,
                            serverUrl: String,
                            val headers : HashMap,
                            monitoredSite: Site,
                            provider: APMProvider) extends BaseEntity{

}

class APMProvider extends Enumeration {
  type APMProvider = Value
  val GRAPHITE, NEWRELIC, APPDYNAMICS = Value
}
