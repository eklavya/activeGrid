package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by babjik on 26/9/16.
 */
case class ResourceACL(override val id: Option[Long]
                       , resources: String = AllResource.name
                       , permission: String = AllPermission.name
                       , resourceIds: Array[Long] = Array.empty) extends BaseEntity

object ResourceACL {
  val label = "ResourceACL"

  implicit class RichResourceACL(resource: ResourceACL) extends Neo4jRep[ResourceACL] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    override def toNeo4jGraph(entity: ResourceACL): Node = {
      val resourceMap = Map("resources" -> entity.resources
        , "permission" -> entity.permission
        , "resourceIds" -> entity.resourceIds
      )
      Neo4jRepository.saveEntity[ResourceACL](label,entity.id,resourceMap)
    }

    override def fromNeo4jGraph(nodeId: Long): Option[ResourceACL] = {
      val node = Neo4jRepository.findNodeById(label,nodeId)
      Neo4jRepository.getProperties(node, "resources", "permission", "resourceIds")
        .map(map => ResourceACL(Some(node.getId)
          , map.get("resources").get.asInstanceOf[String]
          , map.get("permission").get.asInstanceOf[String]
          , map.get("resourceIds").get.asInstanceOf[Array[Long]])).orElse(None)
    }
  }

}