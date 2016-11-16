package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */

case class KeyValueInfo(override val id: Option[Long], key: String, value: String) extends BaseEntity

object KeyValueInfo {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[KeyValueInfo] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "key", "value")
        val key = map("key").toString
        val value = map("value").toString

        Some(KeyValueInfo(Some(nodeId), key, value))
      case None =>
        logger.warn(s"could not find node for KeyValueInfo with nodeId $nodeId")
        None
    }
  }

  implicit class KeyValueInfoImpl(keyValueInfo: KeyValueInfo) extends Neo4jRep[KeyValueInfo] {

    override def toNeo4jGraph(entity: KeyValueInfo): Node = {
      val label = "KeyValueInfo"
      val mapPrimitives = Map("key" -> entity.key, "value" -> entity.value)
      val node = Neo4jRepository.saveEntity[KeyValueInfo](label, entity.id, mapPrimitives)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[KeyValueInfo] = {
      KeyValueInfo.fromNeo4jGraph(id)
    }
  }

}
