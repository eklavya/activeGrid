package com.activegrid.model

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude}
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.{Node, NotFoundException}
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 22/9/16.
  */
@JsonIgnoreProperties(ignoreUnknown = true)
case class ImageInfo(override val id: Option[Long],
                     @JsonInclude(JsonInclude.Include.NON_NULL) state: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) ownerId: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) publicValue: Boolean,
                     @JsonInclude(JsonInclude.Include.NON_NULL) architecture: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) imageType: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) platform: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) imageOwnerAlias: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) name: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) description: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) rootDeviceType: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) rootDeviceName: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) version: String) extends BaseEntity

object ImageInfo {

  implicit class ImageInfoImpl(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ImagesTest2"

    override def toNeo4jGraph(imageInfo: ImageInfo): Node = {
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
      imageInfoNode
    }


    override def fromNeo4jGraph(nodeId: Long): Option[ImageInfo] = {
      ImageInfo.fromNeo4jGraph(nodeId)
    }


  }

  def fromNeo4jGraph(nodeId: Long): Option[ImageInfo] = {
    try {
      val node = GraphDBExecutor.findNodeById(nodeId)
      val map = GraphDBExecutor.getProperties(node, "state", "ownerId", "publicValue", "architecture", "imageType", "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
      val imageInfo = ImageInfo(Some(node.getId),
        map.get("state").get.asInstanceOf[String],
        map.get("ownerId").get.asInstanceOf[String],
        map.get("publicValue").get.asInstanceOf[Boolean],
        map.get("architecture").get.asInstanceOf[String],
        map.get("imageType").get.asInstanceOf[String],
        map.get("platform").get.asInstanceOf[String],
        map.get("imageOwnerAlias").get.asInstanceOf[String],
        map.get("name").get.asInstanceOf[String],
        map.get("description").get.asInstanceOf[String],
        map.get("rootDeviceType").get.asInstanceOf[String],
        map.get("rootDeviceName").get.asInstanceOf[String],
        map.get("version").get.asInstanceOf[String])
      Some(imageInfo)
    } catch {
      case nfe: NotFoundException => None
      case exception: Exception => throw new Exception("Unable to get the Entity")
    }
  }

}