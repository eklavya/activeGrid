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
                     state : String,
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

      val label: String = "ImageInfo"

      val mapPrimitives : Map[String, Any] = Map("imageId" -> entity.imageId,
                                                  "state" -> entity.state,
                                                "ownerId" -> entity.ownerId,
                                                "publicValue" -> entity.publicValue,
                                                "architecture" -> entity.architecture,
                                                "imageType" -> entity.imageType,
                                                "platform"   -> entity.platform,
                                                "imageOwnerAlias" -> entity.imageOwnerAlias,
                                                "name"  -> entity.name,
                                                "description" -> entity.description,
                                                "rootDeviceType" -> entity.rootDeviceName,
                                                "rootDeviceName" -> entity.rootDeviceType,
                                                "version" -> entity.version)

      val node = GraphDBExecutor.createGraphNodeWithPrimitives[ImageInfo](label,mapPrimitives)
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[ImageInfo] = {


      val listOfKeys: List[String] = List("imageId","state", "ownerId", "publicValue", "architecture", "imageType",
        "platform", "imageOwnerAlias", "name", "description", "rootDeviceType", "rootDeviceName", "version")

      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val imageId: String = propertyValues.get("imageId").get.toString
      val state: String = propertyValues.get("state").get.toString
      val ownerId: String =  propertyValues.get("ownerId").get.toString
      val publicValue : Boolean = propertyValues.get("publicValue").get.toString.toBoolean

      val architecture: String = propertyValues.get("architecture").get.toString
      val imageType: String =  propertyValues.get("imageType").get.toString
      val platform : String = propertyValues.get("platform").get.toString
      val imageOwnerAlias: String = propertyValues.get("imageOwnerAlias").get.toString
      val name: String =  propertyValues.get("name").get.toString
      val description : String = propertyValues.get("description").get.toString
      val rootDeviceType: String = propertyValues.get("rootDeviceType").get.toString
      val rootDeviceName: String =  propertyValues.get("rootDeviceType").get.toString
      val version : String = propertyValues.get("version").get.toString

      Some(ImageInfo(Some(nodeId),imageId,state, ownerId, publicValue, architecture, imageType,platform, imageOwnerAlias, name, description, rootDeviceType, rootDeviceName, version))

    }

  }

}