package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by nagulmeeras on 25/10/16.
  */
case class AccountInfo(override val id: Option[Long],
                       accountId: Option[String],
                       providerType: InstanceProvider, // AWS or OpenStack or Physical LAN etc..
                       ownerAlias: Option[String], //optional
                       accessKey: String,
                       secretKey: String,
                       regionName: String, // region or end-point
                       regions: List[String],
                       networkCIDR: String) extends BaseEntity

object AccountInfo {
  val accountInfoLabel = "AccountInfo"
  val logger = LoggerFactory.getLogger(getClass)

  implicit class AccountInfoImpl(accountInfo: AccountInfo) extends Neo4jRep[AccountInfo] {
    logger.debug(s"Executing $getClass :: toNeo4jGraph")

    override def toNeo4jGraph(entity: AccountInfo): Node = {
      val map = Map("providerType" -> entity.providerType.toString,
        "ownerAlias" -> entity.ownerAlias,
        "accessKey" -> entity.accessKey,
        "secretKey" -> entity.secretKey,
        "regionName" -> entity.regionName,
        "regions" -> entity.regions,
        "networkCIDR" -> entity.networkCIDR)
      Neo4jRepository.saveEntity[AccountInfo](accountInfoLabel, entity.id, map)

    }

    override def fromNeo4jGraph(nodeId: Long): Option[AccountInfo] = {
      AccountInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[AccountInfo] = {
    logger.debug(s"Executing $getClass :: fromNeo4jGraph")
    val maybeNode = Neo4jRepository.findNodeById(nodeId)
    maybeNode match {
      case Some(node) =>
        if (Neo4jRepository.hasLabel(node, accountInfoLabel)) {
          val map = Neo4jRepository.getProperties(node, "accountId", "providerType", "ownerAlias", "accessKey", "secretKey", "regionName", "regions", "networkCIDR")
          Some(new AccountInfo(Some(node.getId),
            ActiveGridUtils.getValueFromMapAs[String](map, "accountId"),
            InstanceProvider.toInstanceProvider(map("providerType").asInstanceOf[String]),
            ActiveGridUtils.getValueFromMapAs[String](map, "ownerAlias"),
            map("accessKey").asInstanceOf[String],
            map("secretKey").asInstanceOf[String],
            map("regionName").asInstanceOf[String],
            map("regions").asInstanceOf[List[String]],
            map("networkCIDR").asInstanceOf[String]
          ))
        } else {
          None
        }
      case None => None
    }
  }
}
