package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 28/10/16.
  */
case class Tuple(override val id: Option[Long], key: String, value: String) extends BaseEntity
