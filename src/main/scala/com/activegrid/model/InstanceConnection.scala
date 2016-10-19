package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  *
  */

case class InstanceConnection(override val id: Option[Long],sourceNodeId: String, targetNodeId: String, portRanges: List[PortRange])  extends BaseEntity

object InstanceConnection{

  def fromNeo4jGraph(id: Option[Long]): Option[InstanceConnection] = {
    id match {
      case Some(nodeId) =>
        val listOfKeys = List("sourceNodeId", "targetNodeId")
        val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
        val sourceNodeId = propertyValues("sourceNodeId").toString
        val targetNodeId = propertyValues("targetNodeId").toString
        val relationship = "HAS_portRange"
        val childNodeIds: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship)
        val portRanges: List[PortRange] = childNodeIds.flatMap { childId =>
          PortRange.fromNeo4jGraph(Some(childId))
        }
        Some(InstanceConnection(Some(nodeId), sourceNodeId, targetNodeId, portRanges))

      case None => None
    }
  }

  implicit class InstanceConnectionImpl(instanceConnection: InstanceConnection) extends Neo4jRep[InstanceConnection]{

    override def toNeo4jGraph(entity: InstanceConnection): Node = {

      val label = "InstanceConnection"
      val mapPrimitives  = Map("sourceNodeId" -> entity.sourceNodeId, "targetNodeId" -> entity.targetNodeId)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[InstanceConnection](label, mapPrimitives)
      val relationship = "HAS_portRange"
      entity.portRanges.foreach{portRange =>
        val portRangeNode = portRange.toNeo4jGraph(portRange)
        GraphDBExecutor.setGraphRelationship(node,portRangeNode,relationship)
      }

      node
    }

    override def fromNeo4jGraph(id: Option[Long]): Option[InstanceConnection] = {
      InstanceConnection.fromNeo4jGraph(id)
    }

  }

}
