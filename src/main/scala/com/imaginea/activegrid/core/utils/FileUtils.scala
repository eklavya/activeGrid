package com.imaginea.activegrid.core.utils

import java.io.{File, FileNotFoundException, PrintWriter}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
 * Created by babjik on 5/10/16.
 */
object FileUtils {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def saveFile(file: String, content: String): Unit = {
    val writer = new PrintWriter(new File(file), "UTF-8")
    try {
      writer.print(content)
    } catch {
      case ex: FileNotFoundException => logger.error(ex.getMessage)
      case ex: Exception => logger.error(ex.getMessage)
    } finally {
      writer.close()
    }
  }
}
