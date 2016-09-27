package com.activegrid.utils

import com.activegrid.services.{AppSettingsService, CatalogService}
import akka.http.scaladsl.server.Directives._

/**
  * Created by sivag on 27/9/16.
  */
object EndpointsAggregator {
  val appService = new AppSettingsService()
  val catalogService = new CatalogService()
  val endPoints = appService.routes ~ catalogService.routes
}
