package com.imaginea.activegrid.core.models

import java.util.Base64



/**
  * Created by sivag on 22/12/16.
  */
class AdminManagerImpl {

  /**
    *
    * @param siteId
    * @param instanceId
    * @return APM Server details with given site and instance id
    */
  def getAPMServerByInstance(siteId: Long, instanceId: String): Option[APMServerDetails] = {

    //todo implementation required.
    APMServerDetails.fromNeo4jGraph(0l) // Fake response
  }

  /**
    * Set basic authentication headers in request.
    */
  def setAuthStrategy(): Unit = {
    //todo implementation required
  }

  /**
    *
    * @param baseUri
    * @param siteId
    * @param instanceId
    * @param resouce
    * @return  ResouceUtilization
    *
    */
  //scalastyle:off cyclomatic.complexity method.length
  def fetchMetricData(baseUri: String, siteId: Long, instanceId: String, resouce: String): ResouceUtilization = {
    val resourceUtilization = ResouceUtilization("target",List.empty[DataPoint])
    // Getting application management server.
    val serverDetails = getAPMServerByInstance(siteId, instanceId)
    serverDetails.map {
      sdetails =>
        val instance = SiteManagerImpl.getInstance(siteId, instanceId)
        val providerType = APMProvider.toProvider(instance.provider)
        providerType match {
          case NEWRELIC =>
            val plugIn = PluginManager.getPlugin("apm-newrlic")
            plugIn match {
              case Some(pi) =>
                val url = baseUri.concat("/plugins/{plugin}/servers/{serverId}/metrics".replace("{plugin}",
                  pi.name.replace("{serverId}", sdetails.id.toString())))
                val prop = Map("resouce" -> resouce, "instance" -> instance)
                //todo "getAuthSettingsFor" implementation
                val authStrategy = AppSettings.getAuthSettingsFor("auth.strategy")
                authStrategy match {
                  case "anonymous" =>
                    val apps = "apiuser:password"
                    val ciper : String = "Basic" + Base64.getEncoder.encode(apps.getBytes()).toString
                    val headers = Map("Authorization" ->  ciper)
                    //todo getData implementation
                    val merticData = HttpClient.getData(url, headers, Map.empty[String,String])
                    if (merticData != null && merticData.length > 0) {
                      //new ObjectMapper().readValue(metricData, ResouceUtilization.getClass)
                      //todo simulate above code and extract properties to make ResourceUtilization bean.
                      resourceUtilization
                    }
                }
            }
          case GRAPHITE =>
            val plugIn = PluginManager.getPlugin("apm-graphite")
            plugIn match {
              case Some(pi) =>
                val url = baseUri.concat("/plugins/{plugin}/metrics".replace("{plugin}", pi.name))
                //todo setAuthStrategy implementation
                setAuthStrategy()
                val query: APMQuery = APMQuery("carbon.agents.ip-10-191-186-149-a.cpuUsage", "-1h", "until", "json", sdetails.serverUrl)
                // json format data
                //todo sendDataAsJson implementation
                val metricData2 = HttpClient.sendDataAsJson("put", url, Map.empty, Map.empty, query)
                if (metricData2 != null && metricData2.length > 0) {
                  //todo extract properties to make ResourceUtilization bean.
                  resourceUtilization // dummy properties
                }

            }

          case _ => resourceUtilization
        }
    }
    resourceUtilization
  }
}
