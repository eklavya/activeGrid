package com.imaginea.activegrid.core.utils

import java.io.{File, PrintWriter}

/**
 * Created by babjik on 5/10/16.
 */
object FileUtils {

  def saveFile(file: String, content: String): Unit = {
    val writer = new PrintWriter(new File(file), "UTF-8")
    try {
      writer.print(content)
    } finally {
      writer.close()
    }
  }
}
