package com.imaginea.activegrid.core.models

import akka.http.scaladsl.common.StrictForm.FileData


/**
  * Created by nagulmeeras on 18/10/16.
  */
case class PlugIn(override val id: Option[Long],
                  name: String,
                  active: Boolean,
                  file: FileData) extends BaseEntity

