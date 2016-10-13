package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  *
  */

case class InstanceConnection(override val id: Option[Long],sourceNodeId: String, targetNodeId: String, portRanges: List[PortRange])  extends BaseEntity

object InstanceConnection{

  implicit class InstanceConnectionImpl(instanceConnection: InstanceConnection) extends Neo4jRep[InstanceConnection]{

    override def toNeo4jGraph(entity: InstanceConnection): Option[Node] = {

      val label = "InstanceConnection"

      val mapPrimitives  = Map("sourceNodeId" -> entity.sourceNodeId, "targetNodeId" -> entity.targetNodeId)

      val node: Option[Node] = GraphDBExecutor.createGraphNodeWithPrimitives[InstanceConnection](label, mapPrimitives)

      val relationship = "HAS_portRange"
      entity.portRanges.foreach{portRange =>
        val portRangeNode = portRange.toNeo4jGraph(portRange)
        GraphDBExecutor.setGraphRelationship(node,portRangeNode,relationship)
      }

      node
    }

    override def fromNeo4jGraph(nodeId: Long): InstanceConnection = {

      val listOfKeys = List("sourceNodeId","targetNodeId")
      val propertyValues = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val sourceNodeId = propertyValues.get("sourceNodeId").get.toString
      val targetNodeId = propertyValues.get("targetNodeId").get.toString

      val relationship = "HAS_portRange"
      val childNodeIds: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId,relationship)

      val portRanges: List[PortRange] = childNodeIds.map{ childId =>
        val port:PortRange = null
        port.fromNeo4jGraph(childId)
      }

      InstanceConnection(Some(nodeId), sourceNodeId,targetNodeId,portRanges)
    }

  }

}
