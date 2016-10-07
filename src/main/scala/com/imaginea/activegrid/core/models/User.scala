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

  implicit class RichUser(user: User) extends Neo4jRep[User] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "User"

    override def toNeo4jGraph(entity: User): Option[Node] = {
      logger.debug(s"toGraph for Image ${entity}")
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
      logger.debug(s"node - ${node.get}")

      //publicKeys: List[KeyPairInfo]
      //Relation HAS_publicKeys
      entity.publicKeys.foreach(publicKey => UserUtils.addKeyPair(node.get.getId, publicKey))
      node
    }


    override def fromNeo4jGraph(nodeId: Long): User = {
      val node = Neo4jRepository.findNodeById(nodeId).get
      val map = Neo4jRepository.getProperties(node, "username", "password", "email", "uniqueId", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")

      val keyPairInfoNodes = Neo4jRepository.getNodesWithRelation(node, UserUtils.has_publicKeys)
      val keyPairInfo: KeyPairInfo = null

      val keyPairInfos = keyPairInfoNodes.map(keyPairNode => {
        keyPairInfo.fromNeo4jGraph(keyPairNode.getId)
      })

      val user = User(Some(node.getId),
        map.get("username").get.asInstanceOf[String],
        map.get("password").get.asInstanceOf[String],
        map.get("email").get.asInstanceOf[String],
        map.get("uniqueId").get.asInstanceOf[String],
        keyPairInfos,
        map.get("accountNonExpired").get.asInstanceOf[Boolean],
        map.get("accountNonLocked").get.asInstanceOf[Boolean],
        map.get("credentialsNonExpired").get.asInstanceOf[Boolean],
        map.get("enabled").get.asInstanceOf[Boolean],
        map.get("displayName").get.asInstanceOf[String])

      logger.debug(s"user - ${user}")
      user
    }
  }

}

object UserUtils {
  val keyPairInfo: KeyPairInfo = null
  val has_publicKeys = "HAS_publicKeys"

  def addKeyPair(userId: Long, keyPairInfo: KeyPairInfo): Relationship = {
    val userNode = Neo4jRepository.findNodeById(userId).get
    val publicKeyNode = keyPairInfo.toNeo4jGraph(keyPairInfo)
    Neo4jRepository.createRelation(has_publicKeys, userNode, publicKeyNode.get)
  }

  def getUserKeysDir: String = s"${Constants.getTempDirectoryLocation}${Constants.FILE_SEPARATOR}${Constants.USER_KEYS}"

  def getKeyDirPath(userId: Long): String = s"${getUserKeysDir}${Constants.FILE_SEPARATOR}${userId.toString}${Constants.FILE_SEPARATOR}"

  def getKeyFilePath(userId: Long, keyName: String): String = s"${getKeyDirPath(userId)}${keyName}.pub"

}



