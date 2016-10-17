package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */
case class PortRange(override val id: Option[Long],fromPort: Int, toPort: Int)  extends BaseEntity

object PortRange{

  implicit class PortRangeImpl(portRange: PortRange) extends Neo4jRep[PortRange]{

    override def toNeo4jGraph(entity: PortRange): Option[Node] = {
      val label  = "PortRange"
      val mapPrimitives  = Map("fromPort" -> entity.fromPort, "toPort" -> entity.toPort)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[PortRange](label, mapPrimitives)
      node

    }

    override def fromNeo4jGraph(id: Option[Long]): Option[PortRange] = {
      PortRange.fromNeo4jGraph(id)
    }

  }

  def fromNeo4jGraph(id: Option[Long]): Option[PortRange] = {
    id match {
      case Some(nodeId) => {
        val listOfKeys = List("fromPort", "toPort")
        val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
        val fromPort: Int = propertyValues.get("fromPort").get.toString.toInt
        val toPort: Int = propertyValues.get("toPort").get.toString.toInt

        Some(PortRange(Some(nodeId), fromPort, toPort))
      }
      case None => None
    }
  }

}
