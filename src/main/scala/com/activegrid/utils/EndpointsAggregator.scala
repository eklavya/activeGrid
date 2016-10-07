package com.activegrid.utils

import com.activegrid.services.{AppSettingsService, LogConfigUpdaterService}
import akka.http.scaladsl.server.Directives._

/**
  * Created by sivag on 27/9/16.
  */
object EndpointsAggregator {
  val appService = new AppSettingsService()
  val logService = new LogConfigUpdaterService()
  val endPoints = appService.routes ~ logService.routes
}
