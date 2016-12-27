package com.imaginea.activegrid.core.models

import akka.http.impl.util.Util
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.parboiled2.RuleTrace.Fail
import com.imaginea.Main
import com.typesafe.scalalogging.Logger
import org.apache.http.client.methods.HttpPost
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.Success


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
    *         Sends a http request and process response from given "url"
    */
  val POSTRQ = HttpMethod
  val PUTRQ = "GET"
  val logger = Logger(LoggerFactory.getLogger(HttpClient.getClass.getName))
  val materializer = Main.materializer
  val context = Main.executionContext
  val http = Http(Main.system)

  def sendDataAsJson(method: String, url: String, headers: Map[String, String], queryParams: Map[String, String], query: APMQuery): String = {
    val resp = Http().singleRequest(HttpRequest(HttpMethods.POST,url))
    HttpHeader
    resp.onComplete(httpresponse =>
      httpresponse.map { response =>
        if (response.status.isSuccess()) {

        }
        else {
        }
      })
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
