package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory


/**
 * Created by babjik on 5/10/16.
 */
case class UserGroup(override val id: Option[Long]
                     , name: String
                     , users: Option[Set[User]]
                     , accesses: Option[Set[ResourceACL]]) extends BaseEntity


sealed trait ResponseMessage
case class SuccessResponse(id: String) extends ResponseMessage
object FailureResponse extends ResponseMessage

object UserGroup {
  val label = "UserGroup"

  implicit class RichUserGroup(userGroup: UserGroup) extends Neo4jRep[UserGroup] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    val has_users = "HAS_users"
    val has_resourceAccess = "HAS_resourceAccess"

    override def toNeo4jGraph(userGroup: UserGroup): Option[Node] = {

      logger.debug(s"UserGroup Node saved into db - ${userGroup}")
      val map = Map("name" -> userGroup.name)

      val userGroupNode = Neo4jRepository.saveEntity[UserGroup](UserGroup.label, userGroup.id, map)

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
      val nodeOption = Neo4jRepository.findNodeById(nodeId)

      nodeOption.map(node => {
        logger.debug(s" UserGroupProxy ${node}")

        val userGroupMap = Neo4jRepository.getProperties(node, "name")

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

        val userGroup = UserGroup(
          id = Some(node.getId),
          name = userGroupMap.get("name").get.asInstanceOf[String],
          users = Some(users),
          accesses = Some(resources)
        )
        logger.debug(s"UserGroup - ${userGroup}")
        userGroup
      })
    }
  }

}
