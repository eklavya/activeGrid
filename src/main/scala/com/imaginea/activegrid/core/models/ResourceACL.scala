package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by babjik on 26/9/16.
 */
case class ResourceACL(resources: String = ResourceType.All.toString
                       , permission: String = Permission.All.toString
                       , resourceIds: Array[Long] = Array.empty) extends BaseEntity

object ResourceACLProtocol {
  val label = "ResourceACL"
}

object ResourceACL {

  implicit class RichResourceACL(resource: ResourceACL) extends Neo4jRep2[ResourceACL] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ResourceACL"

    override def toGraph(resource: ResourceACL): Option[Node] =
      Neo4jRepository.saveEntity[ResourceACL](resource,label)

    override def fromGraph(nodeId: Long): Option[ResourceACL] = Neo4jRepository.getEntity[ResourceACL](nodeId)
  }

}