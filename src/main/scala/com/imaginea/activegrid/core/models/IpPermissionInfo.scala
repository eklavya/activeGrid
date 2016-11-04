package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

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
  val ipPermissionLabel = "IpPermissionInfo"
  val logger = LoggerFactory.getLogger(getClass)

  implicit class IpPermissionInfoImpl(ipPermissionInfo: IpPermissionInfo) extends Neo4jRep[IpPermissionInfo] {
    override def toNeo4jGraph(entity: IpPermissionInfo): Node = {
      val map = Map("fromPort" -> entity.fromPort,
        "toPort" -> entity.toPort,
        "ipProtocol" -> entity.ipProtocol,
        "groupIds" -> entity.groupIds,
        "ipRanges" -> entity.ipRanges)
      Neo4jRepository.saveEntity[IpPermissionInfo](ipPermissionLabel, entity.id, map)
    }

    override def fromNeo4jGraph(nodeId: Long): Option[IpPermissionInfo] = {
      IpPermissionInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[IpPermissionInfo] = {
    val maybeNode = Neo4jRepository.findNodeById(nodeId)
    maybeNode match {
      case Some(node) =>
        if (Neo4jRepository.hasLabel(node, ipPermissionLabel)) {
          val map = Neo4jRepository.getProperties(node, "fromPort", "toPort", "ipProtocol", "groupIds", "ipRanges")
          Some(IpPermissionInfo(Some(nodeId),
            map("fromPort").asInstanceOf[Int],
            map("toPort").asInstanceOf[Int],
            IpProtocol.toProtocol(map("ipProtocol").asInstanceOf[String]),
            map("groupIds").asInstanceOf[Set[String]],
            map("ipRanges").asInstanceOf[List[String]]))
        } else {
          None
        }
      case None => None
    }
  }
}
