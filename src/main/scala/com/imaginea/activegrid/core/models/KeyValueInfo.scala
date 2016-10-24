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
    val listOfKeys = List("key", "value")
    val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
    if (propertyValues.nonEmpty) {
      val key = propertyValues("key").toString
      val value = propertyValues("value").toString

      Some(KeyValueInfo(Some(nodeId), key, value))
    }
    else {
      logger.warn(s"could not get graph properties for KeyValueInfo node with $nodeId")
      None
    }
  }

  implicit class KeyValueInfoImpl(keyValueInfo: KeyValueInfo) extends Neo4jRep[KeyValueInfo] {

    override def toNeo4jGraph(entity: KeyValueInfo): Node = {
      val label = "KeyValueInfo"
      val mapPrimitives = Map("key" -> entity.key, "value" -> entity.value)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[KeyValueInfo](label, mapPrimitives)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[KeyValueInfo] = {
      KeyValueInfo.fromNeo4jGraph(id)
    }
  }

}