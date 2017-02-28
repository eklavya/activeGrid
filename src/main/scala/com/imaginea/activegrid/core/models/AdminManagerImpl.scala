package com.imaginea.activegrid.core.models


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.imaginea.Main

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}


/**
  * Created by sivag on 22/12/16.
  */
object AdminManagerImpl {

  implicit val system = Main.system
  implicit val materializer = Main.materializer
  implicit val executionContext = Main.executionContext

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
  def fetchMetricData(baseUri: String, siteId: Long, instanceId: String, resouce: String): List[ResourceUtilization] = {
    val emptyResponse = List(ResourceUtilization("target", List.empty[DataPoint]))
    // Getting application management server.
    val serverDetails = APMServerDetails.getAPMServerByInstance(siteId, instanceId)
    serverDetails.map {
      sdetails =>
        val instancenName = Instance.instanceBySiteAndInstanceID(siteId, instanceId).map(i => i.name).getOrElse("NOT PROVIDED")
        val providerType = sdetails.provider
        providerType match {

          case NEWRELIC =>
            val plugIn = PluginManager.getPluginByName("apm-newrlic")
            plugIn.map {
              pi =>
                val url = baseUri.concat("/plugins/{plugin}/servers/{serverId}/metrics".replace("{plugin}",
                  pi.name).replace("{serverId}", sdetails.id.toString()))
                val prop = Map("resouce" -> resouce, "instance" -> instancenName)
                val authStrategy = AppSettings.getAuthSettingsFor("auth.strategy")
                val headers = getHeadersAsPerAuthStrategy(authStrategy)
                val queryParams = Map.empty[String, String]
                val merticData = HttpClient.getData(url, headers, queryParams)
                unmarshallResponseTo[ResourceUtilization](merticData.getOrElse(""),ResourceUtilization.getClass.getSimpleName)
            }.getOrElse(emptyResponse)
          case GRAPHITE =>
            val plugIn = PluginManager.getPluginByName("apm-graphite")
            plugIn.map {
              pi =>
                val url = baseUri.concat("/plugins/{plugin}/metrics".replace("{plugin}", pi.name))
                val query: APMQuery = APMQuery("carbon.agents.ip-10-191-186-149-a.cpuUsage", "-1h", "until", "json", sdetails.serverUrl)
                val headers = getHeadersAsPerAuthStrategy("anonymous")
                val queryParams = Map.empty[String, String]
                val merticData = HttpClient.sendDataAsJson("put", url, headers, queryParams, query).getOrElse("")
                unmarshallResponseTo[ResourceUtilization](merticData,ResourceUtilization.getClass.getSimpleName)
            }.getOrElse(emptyResponse)
          case _ => emptyResponse
        }
    }.getOrElse(emptyResponse)
  }

  /**
    * Application metrics will describe the running status of application.
    *
    * @param baseUri
    * @param aPMServerDetails
    * @return
    *
    */
  //todo getData and convertResposneToType methods implementation
  def fetchApplicationMetrics(baseUri: String, aPMServerDetails: APMServerDetails): List[Application] = {
    val emptyResponse = List.empty[Application]
    aPMServerDetails.provider match {
      case NEWRELIC => PluginManager.getPluginByName("apm-newrilic").map {
        plugIn =>
          val queryParams = Map.empty[String, String]
          val headers = getHeadersAsPerAuthStrategy("anonymous")
          val url = baseUri.concat("/plugins/{plugin}/servers/{serverId}/applications".replace("{plugin}", plugIn.name).replace("{serverId}", aPMServerDetails.id.getOrElse("0L").toString()))
          val response = HttpClient.getData(url, headers, queryParams).getOrElse("")
          //todo logic that extract data from response and covnert data into application beans.
          unmarshallResponseTo[Application](response,Application.getClass.getSimpleName)
      }.getOrElse(emptyResponse)
      case GRAPHITE => // No procedure implemented.
        val response = "PROCEDURE NOT YET DEFINED"
        unmarshallResponseTo[Application](response,Application.getClass.getSimpleName)
      case _ =>
        emptyResponse
    }
  }

  /**
    * Connfigure the headers required for basic authorization.
    *
    * @param authStrategy
    * @return
    */

  def getHeadersAsPerAuthStrategy(authStrategy: String): Map[String, String] = {
    val emptyHeaders = Map.empty[String, String]
    authStrategy match {
      case "anonymous" =>
        val apps = "apiuser:password"
        //Time being remove, Please Please Uncomment and correct
        //val ciper: String = "Basic" + Base64.getEncoder.encode(apps.getBytes()).toString
        val ciper: String = "Basic";
        val headers = Map("Authorization" -> ciper)
        headers
      case "someotherstrategy" =>
        // configuring headers
        emptyHeaders
      case _ =>
        emptyHeaders
    }
  }

  /**
    * Unmarshall given response to T type. T depends on the given className
    * @param response
    * @return List of  T type objects, Empty list if the response is not valid
    */
  //scalastyle:off magic.number
  def unmarshallResponseTo[T](response: String, clsName:String) :  List[T] = {
    implicit val resouceUtilization = Main.resouceUtilizationFormat
    implicit val applicationFormat = Main.applicationFormat

    if (response.length > 0) {
      val mayBeResult =
        // 'clsName' holds value returned by .getSimpleName on a class
      //  It always  suffixed with '$', To remove it dropRight used
        clsName.dropRight(1) match {
          case "ResourceUtilization" => Unmarshal[String](response).to[ResourceUtilization]
          case "Application" => Unmarshal[String](response).to[Application]
        }
      val timeOut = 5 // Value Need to changed  according to its execution time.
      // mayBeResult holding the unmarsal value embedded into Future object i.e like Future[T],
      // To extract T Await used here
      List(Await.result(mayBeResult, Duration(timeOut, duration.MILLISECONDS)).asInstanceOf[T])
    } else {
      List.empty[T]
    }
  }
}
