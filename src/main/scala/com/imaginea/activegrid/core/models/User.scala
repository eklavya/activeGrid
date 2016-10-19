package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.Constants
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.{Node, Relationship}
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 26/9/16.
  */
case class User(override val id: Option[Long]
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


object User {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit class RichUser(user: User) extends Neo4jRep[User] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "User"

    override def toNeo4jGraph(entity: User): Node = {
      logger.debug(s"toGraph for Image $entity")
      val map: Map[String, Any] = Map("username" -> entity.username,
        "password" -> entity.username,
        "email" -> entity.email,
        "uniqueId" -> entity.uniqueId,
        "accountNonExpired" -> entity.accountNonExpired,
        "accountNonLocked" -> entity.accountNonLocked,
        "credentialsNonExpired" -> entity.credentialsNonExpired,
        "enabled" -> entity.enabled,
        "displayName" -> entity.displayName)

      val node = Neo4jRepository.saveEntity[User](label, entity.id, map)
      logger.debug(s"node - $node")

      //publicKeys: List[KeyPairInfo]
      //Relation HAS_publicKeys
      entity.publicKeys.foreach(publicKey => UserUtils.addKeyPair(node.getId, publicKey))
      node
    }


    override def fromNeo4jGraph(nodeId: Long): Option[User] = {
      User.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[User] = {
    Neo4jRepository.findNodeById(nodeId) match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "username", "password", "email", "uniqueId", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")

        val keyPairInfoNodes = Neo4jRepository.getNodesWithRelation(node, UserUtils.has_publicKeys)

        val keyPairInfos = keyPairInfoNodes.flatMap(keyPairNode => {
          KeyPairInfo.fromNeo4jGraph(keyPairNode.getId)
        })

        val user = User(Some(nodeId),
          map("username").toString,
          map("password").toString,
          map("email").toString,
          map("uniqueId").toString,
          keyPairInfos,
          map("accountNonExpired").asInstanceOf[Boolean],
          map("accountNonLocked").asInstanceOf[Boolean],
          map("credentialsNonExpired").asInstanceOf[Boolean],
          map("enabled").asInstanceOf[Boolean],
          map("displayName").toString)

        logger.debug(s"user - $user")
        Some(user)
      case None => None
    }
  }
}

object UserUtils {
  val has_publicKeys = "HAS_publicKeys"

  def addKeyPair(userId: Long, keyPairInfo: KeyPairInfo): Option[Relationship] = {
    Neo4jRepository.findNodeById(userId) match {
      case Some(userNode) =>
        val publicKeyNode = keyPairInfo.toNeo4jGraph(keyPairInfo)
        Some(Neo4jRepository.createRelation(has_publicKeys, userNode, publicKeyNode))
      case None => None
    }


  }

  def getUserKeysDir: String = s"${Constants.getTempDirectoryLocation}${Constants.FILE_SEPARATOR}${Constants.USER_KEYS}"

  def getKeyDirPath(userId: Long): String = s"$getUserKeysDir${Constants.FILE_SEPARATOR}$userId.toString${Constants.FILE_SEPARATOR}"

  def getKeyFilePath(userId: Long, keyName: String): String = s"${getKeyDirPath(userId)}$keyName.pub"

}



