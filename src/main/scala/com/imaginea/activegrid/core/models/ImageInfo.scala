package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by babjik on 22/9/16.
 */

/*
case class ImageInfo(override val id: Option[Long]
                     , imageId: Option[String] = None
                     , state: Option[String] = None
                     , ownerId: Option[String] = None
                     , publicValue: Boolean
                     , architecture: Option[String] = None
                     , imageType: Option[String] = None
                     , platform: Option[String] = None
                     , imageOwnerAlias: Option[String] = None
                     , name: Option[String] = None
                     , description: Option[String] = None
                     , rootDeviceType: Option[String] = None
                     , rootDeviceName: Option[String] = None
                     , version: Option[String] = None) extends BaseEntity


object ImageInfo {

  implicit class RichImageInfo(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ImageInfo"

    override def toNeo4jGraph(entity: ImageInfo): Option[Node] = {
      logger.debug(s"toGraph for Image ${imageInfo}")
      // TODO: Image fields
      val map: Map[String, Any] = Map()
      val node = Neo4jRepository.saveEntity[ImageInfo](label, entity.id, map)

      logger.debug(s"node - ${node.get}")
      node
    }

    //TODO : Change the Empty string
    override def fromNeo4jGraph(nodeId: Long): Option[ImageInfo] = {
      Some(ImageInfo(id = None, publicValue = false))
    }
  }

}*/
