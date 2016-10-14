package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory


/**
 * Created by babjik on 5/10/16.
 */
case class UserGroup(id: Option[Long]
                     , name: String
                     , users: Option[Set[User]]
                     , accesses: Option[Set[ResourceACL]]) extends BaseEntity

case class UserGroupProxy(name: String) extends BaseEntity

object UserGroupProtocol {
  val labelUserGroup = "UserGroup"
}

sealed trait ResponseMessage

case class SuccessResponse(id: String) extends ResponseMessage

object FailureResponse extends ResponseMessage

object UserGroup {

  implicit class RichUserGroup(userGroup: UserGroup) extends Neo4jRep2[UserGroup] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    val has_users = "HAS_users"
    val has_resourceAccess = "HAS_resourceAccess"

    implicit def toUserGroupProxy(userGroup: UserGroup): UserGroupProxy =
      UserGroupProxy(userGroup.name)

    override def toNeo4jGraph(userGroup: UserGroup): Option[Node] = {

      logger.debug(s"UserGroup toGraph ${userGroup}")
      val userGroupNode: Option[Node] = Neo4jRepository.saveEntity[UserGroupProxy](userGroup,UserGroupProtocol.labelUserGroup)

      logger.debug(s"UserGroupProxy Node saved into db - ${userGroupNode}")
      //val map: Map[String, Any] = Map("name" -> userGroup.name)
      //val node = Neo4jRepository.saveEntity[KeyPairInfo](ResourceACLProtocol.label, userGroup.id, map)

      //map function is used to extract the option value
      //Iterating the users and linking to the UserGroup
      logger.debug(s"UserGroupProxy has relation with Users ${userGroup.users}")
      for {users <- userGroup.users
           user <- users
           ugn <- userGroupNode
           userNode <- user.toNeo4jGraph(user)} {
        Neo4jRepository.createRelation(has_users, ugn, userNode)
      }

      //map function is used to extract the option value
      //Iterating the access and linking to the UserGroup
      logger.debug(s"UserGroupProxy has relation with ResourceACL ${userGroup.accesses}")
      for {accesses <- userGroup.accesses
           resource <- accesses
           ugn <- userGroupNode
           resourceNode <- resource.toNeo4jGraph(resource)} {
        Neo4jRepository.createRelation(has_resourceAccess, ugn, resourceNode)
      }

      userGroupNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[UserGroup] = {
      val nodeOption = Neo4jRepository.fetchNodeById[UserGroupProxy](nodeId)

      nodeOption.fold(ex => None, result => {
        val (node, userProxy) = result
        logger.debug(s" UserGroupProxy ${userProxy}")

        val userNodes = Neo4jRepository.getNodesWithRelation(node, has_users)
        val users: Set[User] = userNodes.map(child => {
          logger.debug(s" UserGroup -> User node ${child}")
          val user: User = null
          user.fromNeo4jGraph(child.getId)
        }).flatten.toSet

        val accessNodes = Neo4jRepository.getNodesWithRelation(node, has_resourceAccess)
        val resources = accessNodes.map(child => {
          logger.debug(s" UserGroup -> Resource node ${child}")
          val resource: ResourceACL = null
          resource.fromNeo4jGraph(child.getId)
        }).flatten.toSet

        val user = userProxy.map(obj => {
          UserGroup(
            id = Some(node.getId),
            name = obj.name,
            users = Some(users),
            accesses = Some(resources)
          )
        })
        logger.debug(s"UserGroup - ${user}")
        user
      })
    }
  }

}
