package com.imaginea.activegrid.core.utils

import com.amazonaws.regions.RegionUtils
import com.imaginea.activegrid.core.models.InstanceProvider
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
  * Created by babjik on 13/10/16.
  */
object ActiveGridUtils {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

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
