package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 22/12/16.
  */
object HttpClient {
  /**
    *
    * @param method
    * @param url
    * @param headers
    * @param queryParams
    * @param query
    * @return
    *         Sends a http request and process response from given "url"
    */
  def sendDataAsJson(method: String, url: String, headers: Map[String, String], queryParams: Map[String, String], query: APMQuery): String = {
    "dummy response" // Returning dummy value
  }

  /**
    *
    * @param url
    * @param headers
    * @param value
    * @return
    *         Fetches response and return json values
    *
    */
  def getData(url: String, headers: Map[String, String], value: Map[String, String]): String = {
    "APM Details" // Returning dummy value
  }

}
