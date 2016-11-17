package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 27/10/16.
  */
case class SecurityGroupInfo(override val id: Option[Long],
                             groupName: Option[String],
                             groupId: Option[String],
                             ownerId: Option[String],
                             description: Option[String],
                             ipPermissions: List[IpPermissionInfo],
                             tags: List[KeyValueInfo]) extends BaseEntity

object SecurityGroupInfo {
  def appply(id: Option[Long]): SecurityGroupInfo = {
    SecurityGroupInfo(id, None, None, None, None, List.empty[IpPermissionInfo], List.empty[KeyValueInfo])
  }

  val securityGroupInfoLabel = "SecurityGroupInfo"
  val securityGroupIpPermissionRelation = "HAS_IP_PERMISSION"
  val securityGroupKeyValueRelation = "HAS_KEY_VALUE"

  implicit class SecurityGroupInfoImpl(securityGroupInfo: SecurityGroupInfo) extends Neo4jRep[SecurityGroupInfo] {
    override def toNeo4jGraph(entity: SecurityGroupInfo): Node = {
      val map = Map("groupName" -> entity.groupName,
        "groupId" -> entity.groupId,
        "ownerId" -> entity.ownerId,
        "description" -> entity.description)
      val node = Neo4jRepository.saveEntity[SecurityGroupInfo](securityGroupInfoLabel, entity.id, map)

      entity.ipPermissions.foreach {
        ipPermission =>
          val ipPermissionNode = ipPermission.toNeo4jGraph(ipPermission)
          Neo4jRepository.createRelation(securityGroupIpPermissionRelation, node, ipPermissionNode)
      }
      entity.tags.foreach {
        tag =>
          val tagNode = tag.toNeo4jGraph(tag)
          Neo4jRepository.createRelation(securityGroupKeyValueRelation, node, tagNode)
      }
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[SecurityGroupInfo] = {
      SecurityGroupInfo.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[SecurityGroupInfo] = {
    val maybeNode = Neo4jRepository.findNodeById(nodeId)
    maybeNode match {
      case Some(node) =>
        if (Neo4jRepository.hasLabel(node, securityGroupInfoLabel)) {
          val map = Neo4jRepository.getProperties(node, "groupName", "groupId", "ownerId", "description")

          val childNodeIdsKeyValueInfo = Neo4jRepository.getChildNodeIds(nodeId, securityGroupKeyValueRelation)
          val keyValueInfos: List[KeyValueInfo] = childNodeIdsKeyValueInfo.flatMap { childId =>
            KeyValueInfo.fromNeo4jGraph(childId)
          }

          val childNodeIdsIpPermissionInfo = Neo4jRepository.getChildNodeIds(nodeId, securityGroupIpPermissionRelation)
          val ipPermissions: List[IpPermissionInfo] = childNodeIdsIpPermissionInfo.flatMap { childId =>
            IpPermissionInfo.fromNeo4jGraph(childId)
          }

          Some(SecurityGroupInfo(Some(nodeId),
            ActiveGridUtils.getValueFromMapAs[String](map, "groupName"),
            ActiveGridUtils.getValueFromMapAs[String](map, "groupId"),
            ActiveGridUtils.getValueFromMapAs[String](map, "ownerId"),
            ActiveGridUtils.getValueFromMapAs[String](map, "description"),
            ipPermissions,
            keyValueInfos))
        } else {
          None
        }
      case None => None
    }
  }
}
