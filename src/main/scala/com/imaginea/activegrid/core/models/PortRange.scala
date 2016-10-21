package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */
case class PortRange(override val id: Option[Long], fromPort: Int, toPort: Int) extends BaseEntity

object PortRange {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[PortRange] = {
    val listOfKeys = List("fromPort", "toPort")
    val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
    if (propertyValues.nonEmpty) {
      val fromPort: Int = propertyValues("fromPort").toString.toInt
      val toPort: Int = propertyValues("toPort").toString.toInt

      Some(PortRange(Some(nodeId), fromPort, toPort))
    }
    else {
      logger.warn(s"could not get graph properties for PortRange node with $nodeId")
      None
    }
  }

  implicit class PortRangeImpl(portRange: PortRange) extends Neo4jRep[PortRange] {

    override def toNeo4jGraph(entity: PortRange): Node = {
      val label = "PortRange"
      val mapPrimitives = Map("fromPort" -> entity.fromPort, "toPort" -> entity.toPort)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[PortRange](label, mapPrimitives)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[PortRange] = {
      PortRange.fromNeo4jGraph(id)
    }
  }
}
