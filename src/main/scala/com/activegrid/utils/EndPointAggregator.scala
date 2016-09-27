package com.activegrid.utils

import com.activegrid.services.{AppSettingsService, CatalogService}

/**
  * Created by sivag on 27/9/16.
  */
object EndpointsAggregator {
  val appService = new AppSettingsService()
  val catalogService = new CatalogService()
  val endPoints = appService.routes ~ catalogService.routes
}
