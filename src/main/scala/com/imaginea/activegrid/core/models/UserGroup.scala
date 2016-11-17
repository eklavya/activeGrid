package com.imaginea.activegrid.core.models

/**
  * Created by ranjithrajd on 25/10/16.
  */

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

case class UserGroup(override val id: Option[Long]
                     , name: String
                     , users: Set[User] = Set.empty
                     , accesses: Set[ResourceACL] = Set.empty) extends BaseEntity


object UserGroup {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val label = "UserGroup"
  val hasUsers = "HAS_users"
  val hasResourceAccess = "HAS_resourceAccess"

  implicit class RichUserGroup(userGroup: UserGroup) extends Neo4jRep[UserGroup] {

    override def toNeo4jGraph(userGroup: UserGroup): Node = {

      logger.debug(s"UserGroup Node saved into db - ${userGroup}")
      val map = Map("name" -> userGroup.name)
      val userGroupNode = Neo4jRepository.saveEntity[UserGroup](UserGroup.label, userGroup.id, map)

      //Iterating the users and linking to the UserGroup
      logger.debug(s"UserGroupProxy has relation with Users ${userGroup.users}")

      userGroup.users.foreach { user =>
        val userNode = user.toNeo4jGraph(user)
        Neo4jRepository.createRelation(hasUsers, userGroupNode, userNode)
      }

      //Iterating the access and linking to the UserGroup
      logger.debug(s"UserGroupProxy has relation with ResourceACL ${userGroup.accesses}")
      userGroup.accesses.foreach { resource =>
        val resourceNode = resource.toNeo4jGraph(resource)
        Neo4jRepository.createRelation(hasResourceAccess, userGroupNode, resourceNode)
      }
      userGroupNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[UserGroup] = {
      UserGroup.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[UserGroup] = {

    val nodeOption = Neo4jRepository.findNodeById(nodeId)

    nodeOption.map(node => {
      val userGroupMap = Neo4jRepository.getProperties(node, "name")

      logger.debug(s" UserProxy get properties $userGroupMap")

      val userNodes = Neo4jRepository.getNodesWithRelation(node, hasUsers)
      val users = userNodes.flatMap(child => {
        logger.debug(s" UserGroup -> User node $child")
        User.fromNeo4jGraph(child.getId)
      }).toSet

      val accessNodes = Neo4jRepository.getNodesWithRelation(node, hasResourceAccess)
      val resources = accessNodes.flatMap(child => {
        logger.debug(s" UserGroup -> Resource node $child")
        ResourceACL.fromNeo4jGraph(child.getId)
      }).toSet


      val userGroup = UserGroup(
        id = Some(node.getId),
        name = userGroupMap("name").toString,
        users = users,
        accesses = resources
      )
      logger.debug(s"UserGroup - $userGroup")
      userGroup
    })
  }
}

