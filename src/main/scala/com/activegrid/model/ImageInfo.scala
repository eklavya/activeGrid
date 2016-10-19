package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 22/9/16.
  */
case class ImageInfo(override val id: Option[Long],
                     imageId: String,
                     state: Option[String],
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

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[ImageInfo] = {
    val listOfKeys = List("imageId", "state", "ownerId", "publicValue", "architecture", "imageType",
      "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
    val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
    if (propertyValues.nonEmpty) {
      val imageId = propertyValues("imageId").toString
      val state = propertyValues.get("state").asInstanceOf[Option[String]]
      val ownerId = propertyValues("ownerId").toString
      val publicValue = propertyValues("publicValue").toString.toBoolean
      val architecture = propertyValues("architecture").toString
      val imageType = propertyValues("imageType").toString
      val platform = propertyValues("platform").toString
      val imageOwnerAlias = propertyValues("imageOwnerAlias").toString
      val name = propertyValues("name").toString
      val description = propertyValues("description").toString
      val rootDeviceType = propertyValues("rootDeviceType").toString
      val rootDeviceName = propertyValues("rootDeviceType").toString
      val version = propertyValues("version").toString

      Some(ImageInfo(Some(nodeId), imageId, state, ownerId, publicValue, architecture, imageType, platform, imageOwnerAlias, name, description, rootDeviceType, rootDeviceName, version))
    }
    else {
      logger.warn(s"could not get graph properties for ImageInfo node with ${nodeId}")
      None
    }
  }

  implicit class ImageInfoImpl(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {

    override def toNeo4jGraph(entity: ImageInfo): Node = {
      val label = "ImageInfo"
      val mapPrimitives = Map("imageId" -> entity.imageId,
        "state" -> entity.state.getOrElse(GraphDBExecutor.NO_VAL),
        "ownerId" -> entity.ownerId,
        "publicValue" -> entity.publicValue,
        "architecture" -> entity.architecture,
        "imageType" -> entity.imageType,
        "platform" -> entity.platform,
        "imageOwnerAlias" -> entity.imageOwnerAlias,
        "name" -> entity.name,
        "description" -> entity.description,
        "rootDeviceType" -> entity.rootDeviceName,
        "rootDeviceName" -> entity.rootDeviceType,
        "version" -> entity.version)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[ImageInfo](label, mapPrimitives)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[ImageInfo] = {
      ImageInfo.fromNeo4jGraph(id)
    }
  }
}