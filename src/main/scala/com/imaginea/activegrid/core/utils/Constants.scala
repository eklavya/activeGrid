package com.imaginea.activegrid.core.utils

import java.io.File

import com.typesafe.config.ConfigFactory

/**
  * Created by babjik on 5/10/16.
  */
object Constants {
  val config = ConfigFactory.load

  val TEMPDIRLOC: String = config.getString("tmp.dir")
  val USERKEYS: String = "userkeys"
  val FILESEPARATOR = File.separator
  val NAMETAGKEY = "Name"

  def getTempDirectoryLocation:String = TEMPDIRLOC
}
