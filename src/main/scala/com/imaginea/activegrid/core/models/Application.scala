package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 28/10/16.
  */
case class Application(override val id: Option[Long],,
                       name: String,
                       description: String,
                       version: String,
                       instaces: List[Instance],
                       software: Software,
                       tiers: List[ApplicationTier],
                       aPMServerDetails: APMServerDetails,
                       responseTime: Double) extends BaseEntity
