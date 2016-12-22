package com.imaginea.activegrid.core.models

import com.amazonaws.services.cloudfront.model.Headers

/**
  * Created by sivag on 22/12/16.
  */
object HttpClient {
  def sendDataAsJson(method: String, url: String, headers: Map[String, String], queryParams: Map[String, String], query: APMQuery) : String = {
    "dummy response"
  }
  def getData(url: String, headers: Map[String, String], value: Map[String,String]): String = {
    "APM Details"
  }

}
