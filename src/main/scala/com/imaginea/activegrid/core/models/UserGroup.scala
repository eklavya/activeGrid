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
 /*implicit val userGroupResourceFormat = jsonFormat3(ResourceACL)
 implicit val userGroupFormat = jsonFormat4(UserGroup)
 implicit val pageUserGroupFormat = jsonFormat(Page[UserGroup], "startIndex", "count", "totalObjects", "objects")*/


object UserGroup {
  implicit class RichUserGroup(userGroup: UserGroup) extends Neo4jRep2[UserGroup] {
    import Implicits._
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    val has_users = "HAS_users"
    val has_resourceAccess = "HAS_resourceAccess"

    implicit def toUserGroupProxy(userGroup: UserGroup) : UserGroupProxy =
      UserGroupProxy(userGroup.name)

    override def toGraph(userGroup: UserGroup): Option[Node] = {
      logger.debug(s"toGraph for UserGroup ${userGroup}")

      val userGroupNode = Neo4jRepository.saveEntity[UserGroupProxy](userGroup,UserGroupProtocol.labelUserGroup)
      logger.debug(s"UserGroup Node - ${userGroupNode.get}")

      val map: Map[String, Any] = Map("name" -> userGroup.name)
      val node = Neo4jRepository.saveEntity[KeyPairInfo](ResourceACLProtocol.label, userGroup.id, map)

      userGroup.users.map(_.foreach(user => {
        val userNode = user.toGraph(user)
        Neo4jRepository.createRelation(has_users, userGroupNode.get, userNode.get)
      }))

      userGroup.accesses.map(_.foreach(resource => {
        val resourceNode : Option[Node] = resource.toGraph(resource)
        Neo4jRepository.createRelation(has_resourceAccess, userGroupNode.get, resourceNode.get)
      }))

      userGroupNode
    }
    override def fromGraph(nodeId: Long): Option[UserGroup] = {
      val node = Neo4jRepository.fetchNodeById(nodeId)
      node.fold(ex => None, node => {
        //val userProxy :Option[UserGroupProxy] = Neo4jRepository.getEntity[UserGroupProxy](nodeId)
        val map = Neo4jRepository.getProperties(node, "name")
        val userNodes = Neo4jRepository.getNodesWithRelation(node, has_users)

        val users = userNodes.map(child => {
          logger.debug(s" UserGroup -> user node ${child}")
          val user: User = null
          user.fromGraph(child.getId)
        }).toSet

        val accessNodes = Neo4jRepository.getNodesWithRelation(node, has_resourceAccess)
        val resources = accessNodes.map(child => {
          logger.debug(s" UserGroup -> Resource node ${child}")
          val resource: ResourceACL = null
          resource.fromGraph(child.getId).get
        }).toSet

        val user = UserGroup(
          id = Some(node.getId),
          name = map.get("name").get.asInstanceOf[String],
          users = Some(users),
          accesses = Some(resources)
        )
        logger.debug(s"UserGroup - ${user}")
        Some(user)
      })
    }
  }

}
