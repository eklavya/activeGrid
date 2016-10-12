package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models.{APMServerDetails, BaseEntity}

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class Application(name: String,
                       description: String,
                       software: Option[Software] = None,
                       version: String,
                       instances: List[Instance] = List.empty,
                       tiers: List[ApplicationTier] = List.empty,
                       apmServer: Option[APMServerDetails] = None,
                       responseTime: Double) extends BaseEntity{
}
case class ApplicationTier(name: String,description: String,
                           instances: List[Instance] = List.empty,
                           apmServer: APMServerDetails) extends BaseEntity