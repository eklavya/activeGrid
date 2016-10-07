package com.activegrid.models

/**
  * Created by sivag on 7/10/16.
  */


import org.apache.log4j._

import scala.concurrent.{ExecutionContext, Future}

class LogConfigUpdater(implicit ex: ExecutionContext) {

  val ROOT: String = "root"

  def getLogger(logger: String): Logger = {
    logger match {
      case ROOT => getLogger(ROOT)
      case _ => getLogger(logger)
    }
  }

  def getLogLevel2(logger: String): Future[String] = Future {
    val log: Logger = getLogger(logger)
    if (log != null) log.getLevel.toString else null
  }

  def setLogLevel(logger: String, tolevel: String): Future[String] = Future {
    val log: Logger = getLogger(logger)
    if (log != null) {
      val level = Level.toLevel(tolevel)
      log.setLevel(level)
    }
    "success"
  }
}