package com.activegrid.model

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 4/10/16.
  */
object CatalogImplicits {

  implicit class RichSoftware(software: Software) extends Neo4jRep[Software] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "SoftwaresTest"

    override def toNeo4jGraph(software: Software): Option[Node] = {
      logger.debug(s"In toGraph for Software: ${software}")
      val map: Map[String, Any] = Map("version" -> software.version,
        "name" -> software.name,
        "provider" -> software.provider,
        "downloadURL" -> software.downloadURL,
        "port" -> software.port,
        "processNames" -> software.processNames.toArray,
        "discoverApplications" -> software.discoverApplications)

      val softwareNode = GraphDBExecutor.saveEntity[Software](label, map)
      softwareNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[Software] = {
      val node = GraphDBExecutor.findNodeById(nodeId)
      val map = GraphDBExecutor.getProperties(node, "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
      val software = Software(Some(nodeId),
        map.get("version").get.asInstanceOf[String],
        map.get("name").get.asInstanceOf[String],
        map.get("provider").get.asInstanceOf[String],
        map.get("downloadURL").get.asInstanceOf[String],
        map.get("port").get.asInstanceOf[String],
        map.get("processNames").get.asInstanceOf[Array[String]].toList,
        map.get("discoverApplications").get.asInstanceOf[Boolean])
      Some(software)
    }
  }

  implicit class RichImageInfo(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ImageInfoTest"

    override def toNeo4jGraph(imageInfo: ImageInfo): Option[Node] = {
      logger.debug(s"In toGraph for Image Info: ${imageInfo}")
      val map: Map[String, Any] = Map("state" -> imageInfo.state,
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
    }
  }

}
