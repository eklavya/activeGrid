package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */
case class SSHAccessInfo(override val id: Option[Long], keyPair: KeyPairInfo, userName: String, port: Int) extends BaseEntity

object SSHAccessInfo {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[SSHAccessInfo] = {
    val listOfKeys = List("userName", "port")
    val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
    if (propertyValues.nonEmpty) {
      val userName = propertyValues("userName").toString
      val port = propertyValues("port").toString.toInt
      val relationship = "HAS_keyPair"
      val keyPairInfo: Option[KeyPairInfo] = GraphDBExecutor.getChildNodeId(nodeId, relationship).flatMap(id=> KeyPairInfo.fromNeo4jGraph(id))

      Some(SSHAccessInfo(Some(nodeId), keyPairInfo.get, userName, port))
    }
    else {
      logger.warn(s"could not get graph properties for node with ${nodeId}")
      None
    }
  }

  implicit class SSHAccessInfoImpl(sshAccessInfo: SSHAccessInfo) extends Neo4jRep[SSHAccessInfo] {

    override def toNeo4jGraph(entity: SSHAccessInfo): Node = {
      val label = "SSHAccessInfo"
      val mapPrimitives = Map("userName" -> entity.userName, "port" -> entity.port)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[SSHAccessInfo](label, mapPrimitives)
      val node2 = entity.keyPair.toNeo4jGraph(entity.keyPair)
      val relationship = "HAS_keyPair"
      GraphDBExecutor.setGraphRelationship(node, node2, relationship)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[SSHAccessInfo] = {
      SSHAccessInfo.fromNeo4jGraph(id)
    }
  }
}