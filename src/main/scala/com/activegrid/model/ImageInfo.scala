package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by shareefn on 22/9/16.
  */
case class ImageInfo(override val id: Option[Long],
                     imageId : String,
                     state : Option[String],
                     ownerId  :String,
                     publicValue :Boolean,
                     architecture  :String,
                     imageType  :String,
                     platform  :String,
                     imageOwnerAlias  :String,
                     name  :String,
                     description  :String,
                     rootDeviceType  :String,
                     rootDeviceName  :String,
                     version  :String) extends BaseEntity

object ImageInfo {

  implicit class ImageInfoImpl(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {

    override def toNeo4jGraph(entity: ImageInfo): Option[Node] = {
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

    override def fromNeo4jGraph(id: Option[Long]): Option[ImageInfo] = {
      ImageInfo.fromNeo4jGraph(id)
    }

  }

  def fromNeo4jGraph(id: Option[Long]): Option[ImageInfo] = {
    id match {
      case Some(nodeId) => {
        val listOfKeys = List("imageId", "state", "ownerId", "publicValue", "architecture", "imageType",
          "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")
        val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
        val imageId = propertyValues.get("imageId").get.toString
        val state = propertyValues.get("state").asInstanceOf[Option[String]]
        val ownerId = propertyValues.get("ownerId").get.toString
        val publicValue = propertyValues.get("publicValue").get.toString.toBoolean
        val architecture = propertyValues.get("architecture").get.toString
        val imageType = propertyValues.get("imageType").get.toString
        val platform = propertyValues.get("platform").get.toString
        val imageOwnerAlias = propertyValues.get("imageOwnerAlias").get.toString
        val name = propertyValues.get("name").get.toString
        val description = propertyValues.get("description").get.toString
        val rootDeviceType = propertyValues.get("rootDeviceType").get.toString
        val rootDeviceName = propertyValues.get("rootDeviceType").get.toString
        val version = propertyValues.get("version").get.toString

        Some(ImageInfo(Some(nodeId), imageId, state, ownerId, publicValue, architecture, imageType, platform, imageOwnerAlias, name, description, rootDeviceType, rootDeviceName, version))
      }
      case None => None
    }
  }

}