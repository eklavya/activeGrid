package com.imaginea.activegrid.core.utils

import java.io.File

import com.typesafe.config.ConfigFactory

/**
  * Created by babjik on 5/10/16.
  */
object Constants {
  val config = ConfigFactory.load

  val TEMP_DIR_LOC: String = config.getString("tmp.dir")
  val USER_KEYS: String = "userkeys"
  val FILE_SEPARATOR = File.separator

  def getTempDirectoryLocation = TEMP_DIR_LOC
}
