package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */
case class PortRange(override val id: Option[Long], fromPort: Int, toPort: Int) extends BaseEntity {
  def containsPort(port: Int): Boolean = port >= fromPort && port <= toPort
}

object PortRange {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[PortRange] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "fromPort", "toPort")
        val fromPort: Int = map("fromPort").toString.toInt
        val toPort: Int = map("toPort").toString.toInt

        Some(PortRange(Some(nodeId), fromPort, toPort))
      case None =>
        logger.warn(s"could not find node for PortRange with nodeId $nodeId")
        None
    }
  }

  implicit class PortRangeImpl(portRange: PortRange) extends Neo4jRep[PortRange] {

    override def toNeo4jGraph(entity: PortRange): Node = {
      val label = "PortRange"
      val mapPrimitives = Map("fromPort" -> entity.fromPort, "toPort" -> entity.toPort)
      val node = Neo4jRepository.saveEntity[PortRange](label, entity.id, mapPrimitives)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[PortRange] = {
      PortRange.fromNeo4jGraph(id)
    }
  }

}