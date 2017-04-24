package com.imaginea.activegrid.core.utils

import java.io.File

import com.typesafe.config.ConfigFactory

/**
  * Created by babjik on 5/10/16.
  */
object Constants {
  val config = ConfigFactory.load

  //scalastyle:off field.name
  val TEMP_DIR_LOC: String = config.getString("tmp.dir")
  val LOG_FILE_DIR: String = config.getString("log.dir")
  val USER_KEYS: String = "userkeys"
  val FILE_SEPARATOR = File.separator
  val NAME_TAG_KEY = "Name"
  val DEFAULT_SESSION_TIMEOUT = 1500
  val LIST_COMMAND = "ls"
  val CD_COMMAND = "cd"
  val GREP_COMMAND = "grep"
  //scalastyle:on field.name

  val tempDirectoryLocation: String = TEMP_DIR_LOC
}
