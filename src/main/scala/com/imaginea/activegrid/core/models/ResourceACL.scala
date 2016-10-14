package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by babjik on 26/9/16.
 */
case class ResourceACL(resources: String = AllResource.name
                       , permission: String = AllPermission.name
                       , resourceIds: Array[Long] = Array.empty) extends BaseEntity

object ResourceACLProtocol {
  val label = "ResourceACL"
}

object ResourceACL {

  implicit class RichResourceACL(resource: ResourceACL) extends Neo4jRep2[ResourceACL] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ResourceACL"

    override def toNeo4jGraph(resource: ResourceACL): Option[Node] =
      Neo4jRepository.saveEntity[ResourceACL](resource,label)

    override def fromNeo4jGraph(nodeId: Long): Option[ResourceACL] = Neo4jRepository.getEntity[ResourceACL](nodeId)
  }

}