package com.activegrid.models

import org.apache.log4j._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by nagulmeeras on 06/10/16.
  */
class LogLevelUpdater(implicit executionContext: ExecutionContext) {
  val ROOT: String = "root"

  def getLogger(logger: String): Logger = {
    logger match {
      case ROOT => Logger.getRootLogger
      case _ => Logger.getLogger(logger)
    }
  }

  def getLogLevel(logger: String): Future[Option[String]] = Future {
    val log: Logger = getLogger(logger)
    if (log != null) Some(log.getLevel.toString) else None
  }

  def setLogLevel(logger: String, tolevel: String): Future[Option[String]] = Future {
    val log: Logger = getLogger(logger)
    if (log != null) {
      val level = Level.toLevel(tolevel)
      log.setLevel(level)
    }
    Some("Success")
  }
}
