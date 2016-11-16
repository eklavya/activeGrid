package com.imaginea.activegrid.core.utils

import com.amazonaws.regions.RegionUtils
import com.imaginea.activegrid.core.models.InstanceProvider
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._// scalastyle:ignore underscore.import

/**
  * Created by babjik on 13/10/16.
  */
object ActiveGridUtils {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val config = ConfigFactory.load
  val HOST = config.getString("http.host")
  val PORT = config.getInt("http.port")
  val DBPATH = config.getString("neo4j.dbpath")

  def getValueFromMapAs[T](map: Map[String, Any], key: String): Option[T] = {
    map.get(key).map(_.asInstanceOf[T])
  }

  def getRegions(instanceProvider: InstanceProvider): List[String] = {
    RegionUtils.getRegions.foldLeft(List.empty[String]) {
      (list, region) =>
        region.getName :: list
    }
  }
}
