/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 22/9/16.
  */
case class ImageInfo(override val id: Option[Long],
                     imageId: Option[String],
                     state: Option[String],
                     ownerId: Option[String],
                     publicValue: Boolean,
                     architecture: Option[String],
                     imageType: Option[String],
                     platform: Option[String],
                     imageOwnerAlias: Option[String],
                     name: Option[String],
                     description: Option[String],
                     rootDeviceType: Option[String],
                     rootDeviceName: Option[String],
                     version: Option[String]) extends BaseEntity

object ImageInfo {

  implicit class ImageInfoImpl(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ImageInfo"

    override def toNeo4jGraph(imageInfo: ImageInfo): Node = {
      logger.debug(s"In toGraph for Image Info: $imageInfo")
      val map = Map("imageId" -> imageInfo.imageId,
        "state" -> imageInfo.state,
        "ownerId" -> imageInfo.ownerId,
        "publicValue" -> imageInfo.publicValue,
        "architecture" -> imageInfo.architecture,
        "imageType" -> imageInfo.imageType,
        "platform" -> imageInfo.platform,
        "imageOwnerAlias" -> imageInfo.imageOwnerAlias,
        "name" -> imageInfo.name,
        "description" -> imageInfo.description,
        "rootDeviceType" -> imageInfo.rootDeviceType,
        "rootDeviceName" -> imageInfo.rootDeviceName,
        "version" -> imageInfo.version
      )

      val imageInfoNode = Neo4jRepository.saveEntity[ImageInfo](label, imageInfo.id, map)
      imageInfoNode
    }


    override def fromNeo4jGraph(nodeId: Long): Option[ImageInfo] = {
      ImageInfo.fromNeo4jGraph(nodeId)
    }


  }

  def fromNeo4jGraph(nodeId: Long): Option[ImageInfo] = {

    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    try {
      val node = Neo4jRepository.findNodeById(nodeId)
      val map = Neo4jRepository.getProperties(node.get, "imageId", "state", "ownerId", "publicValue",
        "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description",
        "rootDeviceType", "rootDeviceName", "version")
      val imageInfo = ImageInfo(Some(node.get.getId),
        ActiveGridUtils.getValueFromMapAs[String](map, "imageId"),
        ActiveGridUtils.getValueFromMapAs[String](map, "state"),
        ActiveGridUtils.getValueFromMapAs[String](map, "ownerId"),
        map("publicValue").asInstanceOf[Boolean],
        ActiveGridUtils.getValueFromMapAs[String](map, "architecture"),
        ActiveGridUtils.getValueFromMapAs[String](map, "imageType"),
        ActiveGridUtils.getValueFromMapAs[String](map, "platform"),
        ActiveGridUtils.getValueFromMapAs[String](map, "imageOwnerAlias"),
        ActiveGridUtils.getValueFromMapAs[String](map, "name"),
        ActiveGridUtils.getValueFromMapAs[String](map, "description"),
        ActiveGridUtils.getValueFromMapAs[String](map, "rootDeviceType"),
        ActiveGridUtils.getValueFromMapAs[String](map, "rootDeviceName"),
        ActiveGridUtils.getValueFromMapAs[String](map, "version"))
      Some(imageInfo)
    } catch {
      case ex: Exception =>
        logger.warn(ex.getMessage, ex)
        None

    }
  }
}
