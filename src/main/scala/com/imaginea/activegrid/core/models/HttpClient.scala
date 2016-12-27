package com.imaginea.activegrid.core.models

import akka.actor.Actor
import akka.http.impl.util.Util
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.parboiled2.RuleTrace.Fail
import akka.util.ByteString
import com.imaginea.Main
import com.typesafe.scalalogging.Logger
import org.apache.http.client.methods.HttpPost
import org.slf4j.LoggerFactory



/**
  * Created by sivag on 22/12/16.
  */
object HttpClient extends Actor {
  /**
    * @param method
    * @param url
    * @param headers
    * @param queryParams
    * @param query
    * @return
    *         Sends a http request and process response from given "url"
    */
  val logger = Logger(LoggerFactory.getLogger(HttpClient.getClass.getName))
  implicit  val materializer = Main.materializer
  implicit val actorSystem = Main.system
  implicit val executionContext = Main.executionContext

  def receive = {
    case HttpResponse(StatusCodes.OK, headers, entity, _) =>
      logger.info("Got response, body: " + entity.dataBytes.runFold(ByteString(""))(_ ++ _))
    case HttpResponse(code, _, _, _) =>
      logger.info("Request failed, response code: " + code)
  }
  def sendDataAsJson(method: String, url: String, headers: Map[String, String], queryParams: Map[String, String], query: APMQuery): String = {

    import akka.pattern.pipe
    Http().singleRequest(HttpRequest(HttpMethods.POST,url)).pipeTo(self)
    "REMOVE WHEN IMPLEMENTATION DONE"
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
