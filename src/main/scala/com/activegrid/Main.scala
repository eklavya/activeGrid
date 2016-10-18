package com.activegrid

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers
import akka.stream.ActorMaterializer
import com.activegrid.models._
import com.activegrid.services.{APMServerDetailsService, AppSettingService}
import org.slf4j.LoggerFactory


object Main extends App with JsonSupport {

  override val logger = LoggerFactory.getLogger(getClass.getName)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val aPMService = new APMServerDetailsService
  val logLevelUpdater = new LogLevelUpdater
  val appSettingRepository = new AppSettingService

  def appSettingServiceRoutes = post {
    path("appsettings") {
      entity(as[AppSettings]) {
        appsetting =>
          onComplete(appSettingRepository.saveAppSettings(appsetting)) {
            case util.Success(response) => complete(StatusCodes.OK, "Done")
            case util.Failure(exception) => {
              logger.error(s"Unable to save App Settings $exception")
              complete(StatusCodes.BadRequest, "Unable to save App Settings")
            }
          }
      }
    }
  } ~ get {
    path("config") {
      val appSettings = appSettingRepository.getAppSettings()
      onComplete(appSettings) {
        case util.Success(response) => complete(StatusCodes.OK, response)
        case util.Failure(exception) => {
          logger.error(s"Unable to get App Settings")
          complete(StatusCodes.BadRequest, "Unable to get App Settings")
        }
      }

    }
  } ~ post {
    path(PathMatchers.separateOnSlashes("config/settings")) {
      entity(as[Map[String, String]]) {
        setting =>
          val resp = appSettingRepository.saveSetting(setting)
          onComplete(resp) {
            case util.Success(response) => complete(StatusCodes.OK, "Done")
            case util.Failure(exception) => {
              logger.error(s"Unable to save the settings $exception")
              complete(StatusCodes.BadRequest, "Unable to save the settings")
            }
          }
      }
    }
  } ~ path(PathMatchers.separateOnSlashes("config/settings")) {
    get {
      val appSettings = appSettingRepository.getSettings()
      onComplete(appSettings) {
        case util.Success(response) => complete(StatusCodes.OK, response)
        case util.Failure(exception) => {
          logger.error(s"Unable to fetch settings $exception")
          complete(StatusCodes.BadRequest, "Unable to fetch the settings")
        }
      }
    }
  } ~ path(PathMatchers.separateOnSlashes("config/settings")) {
    delete {
      entity(as[List[String]]) { list =>
        val isDeleted = appSettingRepository.deleteSettings(list)
        onComplete(isDeleted) {
          case util.Success(response) => complete(StatusCodes.OK, "Done")
          case util.Failure(exception) => {
            logger.error(s"Unable to delete settings $exception")
            complete(StatusCodes.BadRequest, "Unable to delete  settings")
          }
        }
      }
    }
  } ~ path(PathMatchers.separateOnSlashes("config/logs/level")) {
    put {
      entity(as[String]) {
        level =>
          onComplete(logLevelUpdater.setLogLevel(logLevelUpdater.ROOT, level)) {
            case util.Success(response) => complete(StatusCodes.OK, "Done")
            case util.Failure(exception) => {
              logger.error(s"Unable to update log level $exception")
              complete(StatusCodes.BadRequest, "Unable to update log level")
            }
          }

      }
    }
  } ~ path(PathMatchers.separateOnSlashes("config/logs/level")) {
    get {
      val response = logLevelUpdater.getLogLevel(logLevelUpdater.ROOT)
      onComplete(response) {
        case util.Success(response) => complete(StatusCodes.OK, response)
        case util.Failure(exception) => {
          logger.error(s"Unable to get the log level $exception")
          complete(StatusCodes.BadRequest, "Unable to get the log level")
        }
      }
    }
  }

  def apmServiceRoutes = path(PathMatchers.separateOnSlashes("apm")) {
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
  } ~ path(PathMatchers.separateOnSlashes("apm")) {
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
  } ~ path("apm" / LongNumber / "url") {
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
  } ~ path("apm" / IntNumber) {
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


  val routes = appSettingServiceRoutes ~ apmServiceRoutes
  Http().bindAndHandle(routes, "localhost", 8000)
  logger.info(s"Server online at http://localhost:8000")

}

