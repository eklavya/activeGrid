package com.imaginea.activegrid.core.models

import java.util.Base64


/**
  * Created by sivag on 22/12/16.
  */
object AdminManagerImpl {

  /**
    * @param baseUri
    * @param siteId
    * @param instanceId
    * @param resouce
    * @return ResouceUtilization
    *
    */
  //scalastyle:off cyclomatic.complexity method.length
  //todo 'sendDataAsJson' implementation
  def fetchMetricData(baseUri: String, siteId: Long, instanceId: String, resouce: String): Option[ResouceUtilization] = {
    val fakeReturnValue = ResouceUtilization("target", List.empty[DataPoint])
    // Getting application management server.
    val serverDetails = APMServerDetails.getAPMServerByInstance(siteId, instanceId)
    serverDetails.map {
      sdetails =>
        val instancenName = Instance.getInstance(siteId, instanceId).map(i => i.name).getOrElse("NOT PROVIDED")
        val providerType = sdetails.provider
        providerType match {
          case NEWRELIC =>
            val plugIn = PluginManager.getPlugin("apm-newrlic")
            plugIn.map {
              pi =>
                val url = baseUri.concat("/plugins/{plugin}/servers/{serverId}/metrics".replace("{plugin}", pi.name).replace("{serverId}", sdetails.id.toString()))
                val prop = Map("resouce" -> resouce, "instance" -> instancenName)
                val authStrategy = AppSettings.getAuthSettingsFor("auth.strategy")
                val headers = getHeadersAsPerAuthStrategy(authStrategy)
                val queryParams = Map.empty[String, String]
                val merticData = HttpClient.getData(url, headers, queryParams)
                convertResposneToType[ResouceUtilization](merticData,ResouceUtilization.getClass).last
            }.getOrElse(fakeReturnValue)
          case GRAPHITE =>
            val plugIn = PluginManager.getPlugin("apm-graphite")
            plugIn.map {
              pi =>
                val url = baseUri.concat("/plugins/{plugin}/metrics".replace("{plugin}", pi.name))
                val query: APMQuery = APMQuery("carbon.agents.ip-10-191-186-149-a.cpuUsage", "-1h", "until", "json", sdetails.serverUrl)
                val headers = getHeadersAsPerAuthStrategy("anonymous")
                val queryParams = Map.empty[String, String]
                val metricData = HttpClient.sendDataAsJson("put", url, headers, queryParams, query)
                convertResposneToType[ResouceUtilization](metricData,ResouceUtilization.getClass).last
            }.getOrElse(fakeReturnValue)
          case _ => fakeReturnValue
        }
    }
  }

  /**
    * Application metrics will describe the running status of application.
    * @param baseUri
    * @param aPMServerDetails
    * @return
    *
    */
  //todo getData and convertResposneToType methods implementation
  def fetchApplicationMetrics(baseUri: String, aPMServerDetails: APMServerDetails): List[Application] = {
    val dummyResponse = List.empty[Application]
    aPMServerDetails.provider match {
      case NEWRELIC => PluginManager.getPlugin("apm-newrilic").map {
        plugIn =>
          val queryParams = Map.empty[String, String]
          val headers = getHeadersAsPerAuthStrategy("anonymous")
          val url = baseUri.concat("/plugins/{plugin}/servers/{serverId}/applications".replace("{plugin}",plugIn.name).replace("{serverId}", aPMServerDetails.id.getOrElse("0L").toString()))
          val response = HttpClient.getData(url, headers, queryParams)
          //todo logic that extract data from response and covnert data into application beans.
          convertResposneToType[Application](response,Application.getClass)
      }
      case GRAPHITE => // No procedure implemented.
        val response = "PROCEDURE NOT YET DEFINED"
        convertResposneToType[Application](response,Application.getClass)
      case _ =>
        dummyResponse
    }
    dummyResponse
  }

  /**
    * This method will read the response and convert it to specific type.
    * Default implementation would be fasterxml jaxb which converts json to objects of specific types
    * @param response
    * @param clsType
    * @tparam T
    * @return
    *         Parse response to make list of  objects of T type.
    *
    */
  def convertResposneToType[T:Manifest](response:String, clsType:Class[_]) : List[T] = {
    val emptyResponse = List.empty[T]
    if(response.length > 0){
      // logic to parse response and populate values into T's properties.
      emptyResponse
    }
    else {
      emptyResponse
    }
  }

  /**
    * Connfigure the headers required for basic authorization.
    * @param authStrategy
    * @return
    */

  def getHeadersAsPerAuthStrategy(authStrategy:String) : Map[String,String] = {
    val emptyHeaders = Map.empty[String,String]
    authStrategy match {
      case "anonymous" =>
        val apps = "apiuser:password"
        val ciper: String = "Basic" + Base64.getEncoder.encode(apps.getBytes()).toString
        val headers = Map("Authorization" -> ciper)
        headers
      case "someotherstrategy" =>
        // configuring headers
        emptyHeaders
      case _ =>
        emptyHeaders
    }
  }
}
