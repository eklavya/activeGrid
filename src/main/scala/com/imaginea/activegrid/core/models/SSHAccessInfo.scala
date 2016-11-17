package com.imaginea.activegrid.core.models

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
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "userName", "port")
        val userName = map("userName").toString
        val port = map("port").toString.toInt
        val relationship = "HAS_keyPair"
        val keyPairInfo: Option[KeyPairInfo] = Neo4jRepository.getChildNodeId(nodeId, relationship).flatMap(id => KeyPairInfo.fromNeo4jGraph(id))

        Some(SSHAccessInfo(Some(nodeId), keyPairInfo.get, userName, port))
      case None =>
        logger.warn(s"could not find node for SSHAccessInfo with nodeId $nodeId")
        None
    }
  }

  implicit class SSHAccessInfoImpl(sshAccessInfo: SSHAccessInfo) extends Neo4jRep[SSHAccessInfo] {

    override def toNeo4jGraph(entity: SSHAccessInfo): Node = {
      val label = "SSHAccessInfo"
      val mapPrimitives = Map("userName" -> entity.userName, "port" -> entity.port)
      val node = Neo4jRepository.saveEntity[SSHAccessInfo](label, entity.id, mapPrimitives)
      val node2 = entity.keyPair.toNeo4jGraph(entity.keyPair)
      val relationship = "HAS_keyPair"
      Neo4jRepository.setGraphRelationship(node, node2, relationship)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[SSHAccessInfo] = {
      SSHAccessInfo.fromNeo4jGraph(id)
    }
  }

}
