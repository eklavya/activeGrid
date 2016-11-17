package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  *
  */

case class InstanceConnection(override val id: Option[Long],
                              sourceNodeId: String,
                              targetNodeId: String,
                              portRanges: List[PortRange]) extends BaseEntity

object InstanceConnection {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[InstanceConnection] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "sourceNodeId", "targetNodeId")
        val sourceNodeId = map("sourceNodeId").toString
        val targetNodeId = map("targetNodeId").toString
        val relationship = "HAS_portRange"
        val childNodeIds: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship)
        val portRanges: List[PortRange] = childNodeIds.flatMap { childId =>
          PortRange.fromNeo4jGraph(childId)
        }
        Some(InstanceConnection(Some(nodeId), sourceNodeId, targetNodeId, portRanges))
      case None =>
        logger.warn(s"could not find node for InstanceConnection with nodeId $nodeId")
        None
    }
  }

  implicit class InstanceConnectionImpl(instanceConnection: InstanceConnection) extends Neo4jRep[InstanceConnection] {

    override def toNeo4jGraph(entity: InstanceConnection): Node = {
      val label = "InstanceConnection"
      val mapPrimitives = Map("sourceNodeId" -> entity.sourceNodeId, "targetNodeId" -> entity.targetNodeId)
      val node = Neo4jRepository.saveEntity[InstanceConnection](label, entity.id, mapPrimitives)
      val relationship = "HAS_portRange"
      entity.portRanges.foreach { portRange =>
        val portRangeNode = portRange.toNeo4jGraph(portRange)
        Neo4jRepository.setGraphRelationship(node, portRangeNode, relationship)
      }
      node
    }

    override def fromNeo4jGraph(id: Long): Option[InstanceConnection] = {
      InstanceConnection.fromNeo4jGraph(id)
    }
  }

}