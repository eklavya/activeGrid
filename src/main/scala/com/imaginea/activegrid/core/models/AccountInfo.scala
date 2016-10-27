package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by sampathr on 25/10/16.
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
    override def toNeo4jGraph(entity: AccountInfo): Node = {
      repository.withTx {
        neo =>
          val node = repository.createNode(accountInfoLabel)(neo)
          if (entity.providerType != null) node.setProperty("providerType", entity.providerType.toString)
          if (entity.ownerAlias.nonEmpty) node.setProperty("ownerAlias", entity.ownerAlias.get)
          if (entity.accessKey.nonEmpty) node.setProperty("accessKey", entity.accessKey)
          if (entity.secretKey.nonEmpty) node.setProperty("secretKey", entity.secretKey)
          if (entity.regionName.nonEmpty) node.setProperty("regionName", entity.regionName)
          if (entity.regions.nonEmpty) node.setProperty("regions", entity.regions.toArray)
          if (entity.networkCIDR.nonEmpty) node.setProperty("networkCIDR", entity.networkCIDR)

          node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[AccountInfo] = {
      AccountInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[AccountInfo] = {
    repository.withTx {
      neo =>
        val node = repository.getNodeById(nodeId)(neo)
        Some(new AccountInfo(Some(node.getId),
          repository.getProperty[String](node, "accountId"),
          InstanceProvider.toInstanceProvider(repository.getProperty[String](node, "providerType").get),
          repository.getProperty[String](node, "ownerAlias"),
          repository.getProperty[String](node, "accessKey").get,
          repository.getProperty[String](node, "secretKey").get,
          repository.getProperty[String](node, "regionName").get,
          repository.getProperty[List[String]](node, "regions").get,
          repository.getProperty[String](node, "networkCIDR").get
        ))
    }
  }
}
