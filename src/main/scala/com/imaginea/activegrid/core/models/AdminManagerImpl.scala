package com.imaginea.activegrid.core.models

import java.util.Base64


/**
  * Created by sivag on 22/12/16.
  */
object AdminManagerImpl {
  /**
    * @param siteId
    * @param instanceId
    * @return APM Server details with given site and instance id
    */
  def getAPMServerByInstance(siteId: Long, instanceId: String): Option[APMServerDetails] = {

    //todo implementation required.
    APMServerDetails.fromNeo4jGraph(0L) // Fake response
  }

  /**
    * Set basic authentication headers in request.
    */
  def setAuthStrategy(): Unit = {
    //todo implementation required
  }

  /**
    * @param baseUri
    * @param siteId
    * @param instanceId
    * @param resouce
    * @return ResouceUtilization
    *
    */
  //scalastyle:off cyclomatic.complexity method.length
  def fetchMetricData(baseUri: String, siteId: Long, instanceId: String, resouce: String): Option[ResouceUtilization] = {
    val fakeReturnValue = ResouceUtilization("target", List.empty[DataPoint])
    // Getting application management server.
    val serverDetails = getAPMServerByInstance(siteId, instanceId)
    serverDetails.map {
      sdetails =>
        val instance = SiteManagerImpl.getInstance(siteId, instanceId)
        val providerType = APMProvider.toProvider(instance.provider)
        providerType match {
          case NEWRELIC =>
            val plugIn = PluginManager.getPlugin("apm-newrlic")
            plugIn.map {
              pi =>
                val url = baseUri.concat("/plugins/{plugin}/servers/{serverId}/metrics".replace("{plugin}",
                  pi.name.replace("{serverId}", sdetails.id.toString())))
                val prop = Map("resouce" -> resouce, "instance" -> instance)
                //todo "getAuthSettingsFor" implementation
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
                //todo setAuthStrategy implementation
                setAuthStrategy()
                val query: APMQuery = APMQuery("carbon.agents.ip-10-191-186-149-a.cpuUsage", "-1h", "until", "json", sdetails.serverUrl)
                // json format data
                //todo sendDataAsJson implementation
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
    * @param response
    * @param clsType
    * @tparam T
    * @return
    *         Parse response to make list of  objects of T type.
    *
    */
  def convertResposneToType[T:Manifest](response:String, clsType:Class[_]) : List[T] = {
    val emptyResponse = List.empty[T]
    if(response != null && response.length > 0){
      // logic to parse response and populate values into T's properties.
      emptyResponse
    }
    else {
      emptyResponse
    }
  }

  /**
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
