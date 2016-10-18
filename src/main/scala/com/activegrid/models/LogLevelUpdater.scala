package com.activegrid.models

import org.apache.log4j._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by nagulmeeras on 06/10/16.
  */
class LogLevelUpdater(implicit executionContext: ExecutionContext) {

  val ROOT: String = "root"

  def getLogger(loggerName: String): Logger = {
    loggerName match {
      case ROOT => Logger.getRootLogger
      case _ => Logger.getLogger(loggerName)
    }
  }

  def getLogLevel(loggerName: String): Future[Option[String]] = Future {
    val log: Logger = getLogger(loggerName)
    if (log != null) Some(log.getLevel.toString) else None
  }

  def setLogLevel(loggerName: String, tolevel: String): Future[Unit] = Future {
    val log: Logger = getLogger(loggerName)
    if (log != null) {
      val level = Level.toLevel(tolevel)
      log.setLevel(level)
    }
  }
}
