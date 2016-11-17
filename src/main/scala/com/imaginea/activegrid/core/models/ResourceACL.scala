package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by ranjithrajd on 25/10/16.
 */

case class ResourceACL(override val id: Option[Long]
                       , resources: String = ResourceTypeAll.name
                       , permission: String = All.name
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

    override def fromNeo4jGraph(nodeId: Long): Option[ResourceACL] = fromNeo4jGraph(nodeId)
  }

  def fromNeo4jGraph(nodeId: Long): Option[ResourceACL] = {
    val nodeOption = Neo4jRepository.findNodeById(nodeId)
    nodeOption.map { node =>
      val resourceMap = Neo4jRepository.getProperties(node, "resources", "permission", "resourceIds")
      ResourceACL(Some(node.getId)
        , resourceMap("resources").toString
        , resourceMap("permission").toString
        , resourceMap("resourceIds").asInstanceOf[Array[Long]])
    }
  }

}
