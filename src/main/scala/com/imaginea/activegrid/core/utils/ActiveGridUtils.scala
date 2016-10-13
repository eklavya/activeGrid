package com.imaginea.activegrid.core.utils

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 13/10/16.
  */
object ActiveGridUtils {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  def getValueFromMapAs[T](map: Map[String, Any], key:String): Option[T] = {
    map.get(key) match {
      case Some(x) => Some(x.asInstanceOf[T])
      case None => None
    }
  }
}
