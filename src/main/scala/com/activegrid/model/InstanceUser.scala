package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */

case class InstanceUser(override val id: Option[Long],userName: String, publicKeys: List[String])  extends BaseEntity

object InstanceUser{

  def fromNeo4jGraph(id: Option[Long]): Option[InstanceUser] = {
    id match {
      case Some(nodeId) =>
        val listOfKeys = List("userName", "publicKeys")
        val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
        val userName = propertyValues("userName").toString
        val publicKeys = propertyValues("publicKeys").asInstanceOf[Array[String]].toList
        Some(InstanceUser(Some(nodeId), userName, publicKeys))

      case None => None
    }
  }

  implicit class InstanceUserImpl(instanceUser: InstanceUser) extends Neo4jRep[InstanceUser]{

    override def toNeo4jGraph(entity: InstanceUser): Node = {

      val label = "InstanceUser"
      val mapPrimitives  = Map("userName" -> entity.userName, "publicKeys" -> entity.publicKeys.toArray)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[InstanceUser](label, mapPrimitives)

      node
    }

    override def fromNeo4jGraph(id: Option[Long]): Option[InstanceUser] = {
      InstanceUser.fromNeo4jGraph(id)
    }

  }

}


