package com.imaginea.activegrid.core.models

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.Logger
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.{HttpDelete, HttpGet}
import org.apache.http.client.utils.URIBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory


/**
  * Created by sivag on 22/12/16.
  */
object HttpClient {
  /**
    * @param method
    * @param url
    * @param headers
    * @param queryParams
    * @param query
    * @return
    * Sends a http request and process response from given "url"
    */
  val logger = Logger(LoggerFactory.getLogger(HttpClient.getClass.getName))

  def sendDataAsJson(method: String, url: String, headers: Map[String, String],
                     queryParams: Map[String, String], query: APMQuery): String = {
    val connectionTimeOut = 1000 // in microseconds
    val socketTimeOut = 1000 //in microseconds
    getContent(url, connectionTimeOut, socketTimeOut)
    //todo json conversion
  }

  /**
    *
    * @param url
    * @param headers
    * @param value
    * @return
    * Fetches response and return json values
    *
    */
  def getData(url: String, headers: Map[String, String], value: Map[String, String]): String = {
    val connectionTimeOut = 1000 // in microseconds
    val socketTimeOut = 1000 //in microseconds
    getContent(url, connectionTimeOut, socketTimeOut)

  }

  /**
    *
    * @param url
    * @param headers
    * @param queryParams
    * @return
    * Initiates delete request,
    *         1. Use case
    * Refer PolicyService.deletePolicy()
    * This method will be used delete authentication header
    * from APM server's configuration,while deleting specific policy
    *
    */

  def deleteData(url: String, headers: Map[String, String], queryParams: Map[String, String]): String = {
    val uri = new URIBuilder(url)
    for ((param, value) <- queryParams) {
      uri.addParameter(param, value)
    }
    val req = new HttpDelete
    req.setURI(uri.build())
    for ((param, value) <- headers) {
      req.addHeader(param, value)
    }
    val client = buildHttpClient(1000, 1000)
    val httpResponse = client.execute(req)
    readResponse(httpResponse)
  }

  /**
    * Returns the text (content) from a  URL as a String.
    * Returns a blank String if there was a problem.
    * This function will also throw exceptions if there are problems trying
    * to connect to the url.
    *
    * @param url               A complete URL, such as "http://foo.com/bar"
    * @param connectionTimeout The connection timeout, in ms.
    * @param socketTimeout     The socket timeout, in ms.
    */
  def getContent(url: String, connectionTimeout: Int, socketTimeout: Int): String = {
    val httpClient = buildHttpClient(connectionTimeout, socketTimeout)
    val httpResponse = httpClient.execute(new HttpGet(url))
    readResponse(httpResponse)
  }

  /**
    *
    * @param connectionTimeout
    * @param socketTimeout
    * @return
    */
  private def buildHttpClient(connectionTimeout: Int, socketTimeout: Int):
  HttpClient = {
    val builder = HttpClientBuilder.create()
    builder.setConnectionTimeToLive(connectionTimeout.toLong, TimeUnit.MICROSECONDS)
    builder.build()
  }

  /**
    *
    * @param httpResponse
    * @return
    *         Read content from HttpResponse object in string foramat
    */
  def readResponse(httpResponse: HttpResponse): String = {
    val entity = Some(httpResponse.getEntity)
    entity.map {
      entity => val inputStream = entity.getContent
        val content = io.Source.fromInputStream(inputStream).getLines.mkString
        inputStream.close
        content
    }.getOrElse("")
  }
}

