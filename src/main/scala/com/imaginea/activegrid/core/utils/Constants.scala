/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
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
  val NAME_TAG_KEY = "Name"

  def getTempDirectoryLocation: String = TEMP_DIR_LOC
}
