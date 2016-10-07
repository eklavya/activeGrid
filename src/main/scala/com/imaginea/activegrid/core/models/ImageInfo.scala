package com.imaginea.activegrid.core.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by babjik on 22/9/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
case class ImageInfo(id: Option[Long]
                     , imageId: String
                     , state: String
                     , ownerId: String
                     , publicValue: Boolean
                     , architecture: String
                     , imageType: String
                     , platform: String
                     , imageOwnerAlias: String
                     , name: String
                     , description: String
                     , rootDeviceType: String
                     , rootDeviceName: String
                     , version: String) extends BaseEntity


object ImageInfo {

  implicit class RichImageInfo(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ImageInfo"

    override def toGraph(entity: ImageInfo): Option[Node] = {
      logger.debug(s"toGraph for Image ${imageInfo}")
      // TODO: Image fields
      val map: Map[String, Any] = Map()
      val node = Neo4jRepository.saveEntity[ImageInfo](label, entity.id, map)

      logger.debug(s"node - ${node.get}")
      node
    }

    override def fromGraph(nodeId: Long): ImageInfo = {
      ImageInfo(Some(0L), "", "", "", false, "", "", "", "", "", "", "", "", "")
    }
  }

}