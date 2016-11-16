package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node

import scala.collection.JavaConversions._

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

  val repository = Neo4jRepository
  val securityGroupInfoLabel = "SecurityGroupInfo"
  val securityGroup_IpPermission_Relation = "HAS_IP_PERMISSION"
  val securityGroup_KeyValue_Relation = "HAS_KEY_VALUE"

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
          repository.createRelation(securityGroup_IpPermission_Relation, node, ipPermissionNode)
      }
      entity.tags.foreach {
        tag =>
          val tagNode = tag.toNeo4jGraph(tag)
          repository.createRelation(securityGroup_KeyValue_Relation, node, tagNode)
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
          val keyValAndIpPermission = node.getRelationships.foldLeft((List.empty[IpPermissionInfo], List.empty[KeyValueInfo])) {
            (result, relationship) =>
              val childNode = relationship.getEndNode
              relationship.getType.name match {
                case `securityGroup_KeyValue_Relation` => (result._1, KeyValueInfo.fromNeo4jGraph(childNode.getId).get :: result._2)
                case `securityGroup_IpPermission_Relation` => (IpPermissionInfo.fromNeo4jGraph(childNode.getId).get :: result._1, result._2)
              }
          }
          Some(SecurityGroupInfo(Some(nodeId),
            ActiveGridUtils.getValueFromMapAs[String](map, "groupName"),
            ActiveGridUtils.getValueFromMapAs[String](map, "groupId"),
            ActiveGridUtils.getValueFromMapAs[String](map, "ownerId"),
            ActiveGridUtils.getValueFromMapAs[String](map, "description"),
            keyValAndIpPermission._1,
            keyValAndIpPermission._2))
        } else {
          None
        }
      case None => None
    }
  }
}
