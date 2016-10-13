package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */
case class SSHAccessInfo(override val id: Option[Long],keyPair : KeyPairInfo, userName: String, port: Int)  extends BaseEntity

object SSHAccessInfo{

  implicit class SSHAccessInfoImpl(sshAccessInfo: SSHAccessInfo) extends Neo4jRep[SSHAccessInfo] {

    override def toNeo4jGraph(entity: SSHAccessInfo): Option[Node] = {

      val label = "SSHAccessInfo"

      val mapPrimitives  = Map("userName" -> entity.userName, "port" -> entity.port)

      val node: Option[Node] = GraphDBExecutor.createGraphNodeWithPrimitives[SSHAccessInfo](label, mapPrimitives)

      val node2: Option[Node] = entity.keyPair.toNeo4jGraph(entity.keyPair)

      val relationship = "HAS_keyPair"
      GraphDBExecutor.setGraphRelationship(node,node2,relationship)

      node

    }

    override def fromNeo4jGraph(nodeId: Long): SSHAccessInfo = {

      val listOfKeys  = List("userName","port")

      val propertyValues = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val userName = propertyValues.get("userName").get.toString
      val port  = propertyValues.get("port").get.toString.toInt

      val relationship = "HAS_keyPair"
      val childNodeId = GraphDBExecutor.getChildNodeId(nodeId,relationship)

      val keyPair:KeyPairInfo = null
      val keyPairInfo: KeyPairInfo = keyPair.fromNeo4jGraph(childNodeId)
      SSHAccessInfo(Some(nodeId),keyPairInfo, userName, port)

    }

  }

}

