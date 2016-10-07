package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.{Node, Relationship}
import org.slf4j.LoggerFactory

/**
 * Created by babjik on 26/9/16.
 */
case class User(id: Option[Long]
                , username: String
                , password: String
                , email: String
                , uniqueId: String
                , publicKeys: List[KeyPairInfo]
                , accountNonExpired: Boolean
                , accountNonLocked: Boolean
                , credentialsNonExpired: Boolean
                , enabled: Boolean
                , displayName: String)
  extends BaseEntity

case class UserProxy(
                      username: String
                      , password: String
                      , email: String
                      , uniqueId: String
                      , accountNonExpired: Boolean
                      , accountNonLocked: Boolean
                      , credentialsNonExpired: Boolean
                      , enabled: Boolean
                      , displayName: String)
  extends BaseEntity

object User {

  implicit class RichUser(user: User) extends Neo4jRep2[User] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "User"

    implicit def toUserProxy(user: User) =
      new UserProxy(
        user.username,
        user.password,
        user.email,
        user.uniqueId,
        user.accountNonExpired,
        user.accountNonLocked,
        user.credentialsNonExpired,
        user.enabled,
        user.displayName
      )

    override def toGraph(entity: User): Option[Node] = {
      logger.debug(s"toGraph for Image ${entity}")

      val node = Neo4jRepository.saveEntity[UserProxy](entity,label)
      logger.debug(s"node - ${node.get}")

      //publicKeys: List[KeyPairInfo]
      //Relation HAS_publicKeys
      entity.publicKeys.foreach(publicKey => UserOperations.addKeyPair(node.get.getId, publicKey))
      node
    }

    override def fromGraph(nodeId: Long): Option[User] = {
      val nodeOption = Neo4jRepository.fetchNodeById[UserProxy](nodeId)
      nodeOption.fold(ex => None,
        result => {
          //val map = Neo4jRepository.getProperties(node, "username", "password", "email", "uniqueId", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")
          val (node, entity) = result
          val keyPairInfoNodes = Neo4jRepository.getNodesWithRelation(node, UserOperations.has_publicKeys)
          val keyPairInfo: KeyPairInfo = null

          val keyPairInfos = keyPairInfoNodes.map(keyPairNode => {
            keyPairInfo.fromGraph(keyPairNode.getId)
          })

          val user = entity.map(userProxy => {
            new User(Some(node.getId),
              userProxy.username,
              userProxy.password,
              userProxy.email,
              userProxy.uniqueId,
              keyPairInfos,
              userProxy.accountNonExpired,
              userProxy.accountNonLocked,
              userProxy.credentialsNonExpired,
              userProxy.enabled,
              userProxy.displayName)
          })
          logger.debug(s"User - ${user}")
          user
        })
    }
  }

}

object UserOperations {
  val keyPairInfo: KeyPairInfo = null
  val has_publicKeys = "HAS_publicKeys"

  def addKeyPair(userId: Long, keyPairInfo: KeyPairInfo): Relationship = {
    val userNode = Neo4jRepository.findNodeById(userId).get
    val publicKeyNode = keyPairInfo.toGraph(keyPairInfo)
    Neo4jRepository.createRelation(has_publicKeys, userNode, publicKeyNode.get)
  }
}



