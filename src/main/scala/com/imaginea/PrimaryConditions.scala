package com.imaginea

import com.imaginea.activegrid.core.models.{Application, BaseEntity}

/**
  * Created by sivag on 28/10/16.
  */
case class PrimaryConditions(override val id:Option[Long],application:Application) extends BaseEntity