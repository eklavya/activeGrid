package com.activegrid.services

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers
import com.activegrid.models.{APMServerDetails, APMServerDetailsReposiory, JsonSupport}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
  * Created by nagulmeeras on 14/10/16.
  */
class APMService(implicit executionContext: ExecutionContext) extends JsonSupport {
  override val logger = LoggerFactory.getLogger(getClass)
  val aPMService = new APMServerDetailsReposiory
  val configureServer = path(PathMatchers.separateOnSlashes("apm")) {
    post {
      entity(as[APMServerDetails]) { apmServerDetails =>
        val serverDetails = aPMService.saveAPMServerDetails(apmServerDetails)
        onComplete(serverDetails) {
          case util.Success(response) => complete(StatusCodes.OK, response)
          case util.Failure(exception) => {
            logger.error(s"Unable to save the APM Server Details $exception")
            complete(StatusCodes.BadRequest, "Unable to save the Server details")
          }
        }
      }
    }
  }

  val getAPMList = path(PathMatchers.separateOnSlashes("apm")) {
    get {
      val serverDetailsList = aPMService.getAPMServersList()
      onComplete(serverDetailsList) {
        case util.Success(response) => complete(StatusCodes.OK, response)
        case util.Failure(exception) => {
          logger.error(s"Unable get the APM Server Details $exception")
          complete(StatusCodes.BadRequest, "Unable get the APM server details")
        }
      }
    }
  }
  val getAPMServerUrl = path("apm" / LongNumber / "url") {
    serverId =>
      get {
        logger.info(s"getting into request context : $serverId")
        val serverDetailsList = aPMService.getAPMServerUrl(serverId)
        onComplete(serverDetailsList) {
          case util.Success(response) => complete(StatusCodes.OK, response)
          case util.Failure(exception) => {
            logger.error(s"Unable to get the APM Server Url $exception")
            complete(StatusCodes.BadRequest, "Unable to get the APM Server Url")
          }
        }
      }
  }

  val getAPMServersBySite = path("apm" / IntNumber) {
    siteId => get {
      val serverDetails = aPMService.getAPMServerBySite(siteId)
      onComplete(serverDetails) {
        case util.Success(response) => complete(StatusCodes.OK, response)
        case util.Failure(exception) =>
          logger.error(s"Unable to get the APM Server Details : ${exception.getMessage}")
          complete(StatusCodes.BadRequest, exception.getMessage)
      }
    }
  }

  val apmServiceRoutes = configureServer ~ getAPMList ~ getAPMServerUrl ~ getAPMServersBySite
}
