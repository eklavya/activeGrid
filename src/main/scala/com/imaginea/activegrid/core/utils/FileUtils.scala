/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.utils

import java.io.{File, PrintWriter}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 5/10/16.
  */
object FileUtils {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def saveContentToFile(file: String, content: String): Unit = {
    logger.debug(s"writing content to $file")
    val writer = new PrintWriter(new File(file), "UTF-8")
    try {
      writer.print(content)
    } finally {
      writer.close()
    }
  }

  def createDirectories(dirName: String): Unit = {
    logger.debug(s"Checking for dir -  $dirName")
    val file = new File(dirName)
    if (!file.exists()) {
      logger.info(s"Creating new dir $dirName")
      new File(dirName).mkdirs()
    }
  }

  //TODO: add definition to change file permissions [600] for keys
}
