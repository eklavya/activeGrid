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


object UserGroup {

  implicit class RichUserGroup(userGroup: UserGroup) extends Neo4jRep[UserGroup] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "UserGroup"

    override def toNeo4jGraph(entity: UserGroup): Option[Node] = {
      val map: Map[String, Any] = Map("name" -> entity.name)

      val node = Neo4jRepository.saveEntity[KeyPairInfo](label, entity.id, map)

      logger.debug(s"user group - ${node.get}")
      node
    }

    override def fromNeo4jGraph(nodeId: Long): UserGroup = {
      val node = Neo4jRepository.findNodeById(nodeId).get
      val map = Neo4jRepository.getProperties(node, "name")
      UserGroup(Some(nodeId), map.get("name").asInstanceOf[String], None, None)
    }
  }

}
