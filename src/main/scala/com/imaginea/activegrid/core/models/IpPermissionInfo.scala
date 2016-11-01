package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class IpPermissionInfo(override val id: Option[Long],
                            fromPort: Int,
                            toPort: Int,
                            ipProtocol: IpProtocol,
                            groupIds: Set[String],
                            ipRanges: List[String]) extends BaseEntity

object IpPermissionInfo {
  val repository = Neo4jRepository
  val ipPermissionLabel = "IpPermissionInfo"

  implicit class IpPermissionInfoImpl(ipPermissionInfo: IpPermissionInfo) extends Neo4jRep[IpPermissionInfo] {
    override def toNeo4jGraph(entity: IpPermissionInfo): Node = {
      repository.withTx {
        neo =>
          val node = repository.createNode(ipPermissionLabel) (neo)
          node.setProperty("fromPort", entity.fromPort)
          node.setProperty("toPort", entity.toPort)
          node.setProperty("ipProtocol", entity.ipProtocol.value)
          node.setProperty("groupIds", entity.groupIds.toArray)
          node.setProperty("ipRanges", entity.ipRanges.toArray)
          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[IpPermissionInfo] = {
      IpPermissionInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[IpPermissionInfo] = {
    repository.withTx {
      neo =>
        val node = repository.getNodeById(nodeId) (neo)
        if (repository.hasLabel(node, ipPermissionLabel)) {
          Some(IpPermissionInfo(Some(nodeId),
            repository.getProperty[Int](node, "fromPort").get,
            repository.getProperty[Int](node, "oPort").get,
            IpProtocol.toProtocol(repository.getProperty[String](node, "ipProtocol").get),
            repository.getProperty[Set[String]](node, "fromPort").get,
            repository.getProperty[List[String]](node, "fromPort").get))
        } else {
          None
        }
    }
  }
}
