package com.activegrid.model

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.{Node, NotFoundException}
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 22/9/16.
  */
case class ImageInfo(override val id: Option[Long],
                     state: String,
                     ownerId: String,
                     publicValue: Boolean,
                     architecture: String,
                     imageType: String,
                     platform: String,
                     imageOwnerAlias: String,
                     name: String,
                     description: String,
                     rootDeviceType: String,
                     rootDeviceName: String,
                     version: String) extends BaseEntity

object ImageInfo {

  implicit class ImageInfoImpl(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ImagesTest2"

    override def toNeo4jGraph(imageInfo: ImageInfo): Option[Node] = {
      logger.debug(s"In toGraph for Image Info: $imageInfo")
      val map = Map("state" -> imageInfo.state,
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

      val imageInfoNode = GraphDBExecutor.saveEntity[ImageInfo](label, map)
      Some(imageInfoNode)
    }


    override def fromNeo4jGraph(nodeId: Long): Option[ImageInfo] = {
      ImageInfo.fromNeo4jGraph(nodeId)
    }


  }

  def fromNeo4jGraph(nodeId: Long): Option[ImageInfo] = {
    try {
      val node = GraphDBExecutor.findNodeById(nodeId)
      val map = GraphDBExecutor.getProperties(node.get, "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
      val imageInfo = ImageInfo(Some(node.get.getId),
        map("state").asInstanceOf[String],
        map("ownerId").asInstanceOf[String],
        map("publicValue").asInstanceOf[Boolean],
        map("architecture").asInstanceOf[String],
        map("imageType").asInstanceOf[String],
        map("platform").asInstanceOf[String],
        map("imageOwnerAlias").asInstanceOf[String],
        map("name").asInstanceOf[String],
        map("description").asInstanceOf[String],
        map("rootDeviceType").asInstanceOf[String],
        map("rootDeviceName").asInstanceOf[String],
        map("version").asInstanceOf[String])
      Some(imageInfo)
    } catch {
      case nfe: NotFoundException => None
      case exception: Exception => throw new Exception("Unable to get the Entity")
    }
  }

}