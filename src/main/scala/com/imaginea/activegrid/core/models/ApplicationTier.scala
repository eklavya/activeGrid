package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 28/10/16.
  */
case class ApplicationTier(override val id: Option[Long],
                           name:String,
                           description:String,
                           instances:List[Instance],
                           apmServer: APMServerDetails) extends BaseEntity