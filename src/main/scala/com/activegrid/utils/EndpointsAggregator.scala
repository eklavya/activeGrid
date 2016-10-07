package com.activegrid.utils

import akka.http.scaladsl.server.Directives._
import com.activegrid.services.{AppSettingsService, LogConfigUpdaterService}

/**
  * Created by sivag on 27/9/16.
  */
object EndpointsAggregator {
  val appService = new AppSettingsService()
  val logService = new LogConfigUpdaterService()
  val endPoints = appService.routes ~ logService.routes
}
