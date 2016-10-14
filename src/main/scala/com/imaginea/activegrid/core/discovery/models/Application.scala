package com.imaginea.activegrid.core.discovery.models

import com.imaginea.activegrid.core.models.{APMServerDetails, BaseEntity}

/**
 * Created by ranjithrajd on 12/10/16.
 */
case class Application(override val id: Option[Long]
                       , name: String
                       , description: String
                       , software: Option[Software] = None
                       , version: String
                       , instances: List[Instance] = List.empty
                       , tiers: List[ApplicationTier] = List.empty
                       , apmServer: Option[APMServerDetails] = None
                       , responseTime: Double) extends BaseEntity {
}

case class ApplicationTier(override val id: Option[Long]
                           , name: String
                           , description: String
                           , instances: List[Instance] = List.empty
                           , apmServer: APMServerDetails) extends BaseEntity