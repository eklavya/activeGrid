package com.imaginea.activegrid.core.models

import java.util.Base64

import com.fasterxml.jackson.databind.ObjectMapper


/**
  * Created by sivag on 22/12/16.
  */
class AdminManagerImpl {

  def getAPMServerByInstance(siteId: Long, instanceId: String): Option[APMServerDetails] = {
    //todo implementation required.
  }

  def setAuthStrategy() : Unit = {

  }
  def fetchMetricData(baseUri: String, siteId: Long, instanceId: String, resouce: String): ResouceUtilization = {
    val serverDetails = getAPMServerByInstance(siteId, instanceId)
    serverDetails.map {
      sdetails =>
        val instanceName = SiteManagerImpl.getInstance(siteId, instanceId)
        val provider = APMProvider.toProvider(instanceName.provider)
        provider match {
          case NEWRELIC =>
            val plugIn = PluginManager.getPlugin("apm-newrlic")
            plugIn match {
              case Some(pi) =>
                val url = baseUri.concat("/plugins/{plugin}/servers/{serverId}/metrics".replace("{plugin}", pi.name.replace("{serverId}", sdetails.id.toString())))
                val prop = Map("resouce" -> resouce, "instance" -> instanceName)
                val authStrategy = SiteManagerImpl.getAuthSettingsFor("auth.strategy")
                authStrategy match {
                  case "ananymous" =>
                    val apps = "apiuser:password"
                    val encodeApp = Base64.getEncoder.encode(apps.getBytes())
                    val headers = Map("Authorization", "Basic " + encodeApp)
                    val merticData = HttpClient.getData(url, headers, null)
                    if (merticData != null && merticData.length > 0) {
                      val mapper = new ObjectMapper().readValue(merticData, ResouceUtilization.getClass)
                      mapper
                    }
                }
            }
          case GRAPHITE =>
            val plugIn = PluginManager.getPlugin("apm-graphite")
            plugIn match {
              case Some(pi) =>
                val url = baseUri.concat("/plugins/{plugin}/metrics".replace("{plugin}", pi.name))
                setAuthStrategy()
                APMQuery

            }

          case _ =>
        }
    }

  }
