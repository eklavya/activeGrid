package com.activegrid.model

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */

case class InstanceUser(override val id: Option[Long], userName: String, publicKeys: List[String]) extends BaseEntity

object InstanceUser {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[InstanceUser] = {
    val listOfKeys = List("userName", "publicKeys")
    val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
    if (propertyValues.nonEmpty) {
      val userName = propertyValues("userName").toString
      val publicKeys = propertyValues("publicKeys").asInstanceOf[Array[String]].toList

      Some(InstanceUser(Some(nodeId), userName, publicKeys))
    }
    else {
      logger.warn(s"could not get graph properties for node with $nodeId")
      None
    }
  }

  implicit class InstanceUserImpl(instanceUser: InstanceUser) extends Neo4jRep[InstanceUser] {

    override def toNeo4jGraph(entity: InstanceUser): Node = {
      val label = "InstanceUser"
      val mapPrimitives = Map("userName" -> entity.userName, "publicKeys" -> entity.publicKeys.toArray)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[InstanceUser](label, mapPrimitives)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[InstanceUser] = {
      InstanceUser.fromNeo4jGraph(id)
    }
  }
}
