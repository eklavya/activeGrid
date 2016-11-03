package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
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
  val repository = Neo4jRepository
  val accountInfoLabel = "AccountInfo"

  implicit class AccountInfoImpl(accountInfo: AccountInfo) extends Neo4jRep[AccountInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "AccountInfo"

    override def toNeo4jGraph(accountInfo: AccountInfo): Node = {
      logger.debug(s"In toGraph for AccountInfo: $accountInfo")
      val map = Map("accountId" -> accountInfo.accountId,
        "providerType" -> accountInfo.providerType,
        "ownerAlias" -> accountInfo.ownerAlias,
        "accessKey" -> accountInfo.accessKey,
        "secretKey" -> accountInfo.secretKey,
        "regionName" -> accountInfo.regionName,
        "regions" -> accountInfo.regions.toArray,
        "networkCIDR" -> accountInfo.networkCIDR)

      val accountInfoNode = GraphDBExecutor.saveEntity[AccountInfo](label, map)
      accountInfoNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[AccountInfo] = {
      AccountInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[AccountInfo] = {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    try {
      val node = GraphDBExecutor.findNodeById(nodeId)
      val map = GraphDBExecutor.getProperties(node.get, "accountId", "providerType", "ownerAlias", "accessKey", "secretKey", "regionName", "regions", "networkCIDR")

      val accountInfo = AccountInfo(Some(nodeId),
        ActiveGridUtils.getValueFromMapAs[String](map, "accountId"),
        InstanceProvider.toInstanceProvider(map("providerType").asInstanceOf[String]),
        ActiveGridUtils.getValueFromMapAs[String](map, "ownerAlias"),
        map("accessKey").asInstanceOf[String],
        map("secretKey").asInstanceOf[String],
        map("regionName").asInstanceOf[String],
        map("regions").asInstanceOf[Array[String]].toList,
        map("networkCIDR").asInstanceOf[String])
      Some(accountInfo)
    } catch {
      case ex: Exception =>
        logger.warn(ex.getMessage, ex)
        None
    }
  }
}
