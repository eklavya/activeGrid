package com.activegrid.models

/**
  * Created by sivag on 7/10/16.
  */


import org.apache.log4j._
import org.neo4j.helpers.Exceptions

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LogConfigUpdater {

  val ROOT: String = "root" //

  def getLogger(loggerName: String): Logger = {
    loggerName match {
      case ROOT => Logger.getRootLogger
      case _ => Logger.getLogger(loggerName)
    }
  }

  def getLogLevel(loggerName: String): Future[String] = Future {
    val logger: Logger = getLogger(loggerName)
    if (logger != null)
      logger.getLevel.toString
    else
      null
  }

  def setLogLevel(loggerName: String, tolevel: String): Future[ExecutionStatus] = Future {
    val log: Logger = getLogger(loggerName)
    if (log != null) {
      val level = Level.toLevel(tolevel)
      log.setLevel(level)
      ExecutionStatus(true)
    }
    else {
      ExecutionStatus(false)
    }

  }
}


