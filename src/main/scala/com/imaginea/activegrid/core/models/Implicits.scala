package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.models.KeyPairStatus.KeyPairStatus
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 3/10/16.
  */
object Implicits {

  implicit class RichImageInfo(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ImageInfo"

    override def toGraph(imageInfo: ImageInfo): Option[Node] = {
      logger.debug(s"toGraph for Image ${imageInfo}")
      // TODO: Image fields
      val map: Map[String, Any] = Map()
      val node = Neo4jRepository.saveEntity[ImageInfo](label, map)

      logger.debug(s"node - ${node.get}")
      node
    }

    override def fromGraph(nodeId: Long): ImageInfo = {
      ImageInfo("", "", "", false, "", "", "", "", "", "", "", "", "")
    }
  }


  implicit class RichKeyPairInfo(keyPairInfo: KeyPairInfo) extends Neo4jRep[KeyPairInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "KeyPairInfo"

    override def toGraph(entity: KeyPairInfo): Option[Node] = {
      logger.debug(s"toGraph for KeyPairInfo ${entity}")
      val map: Map[String, Any] = Map(
        "keyName" -> entity.keyName,
        "keyFingerprint" -> entity.keyFingerprint,
        "keyMaterial" -> entity.keyMaterial,
        "filePath" -> entity.filePath,
        "status" -> entity.status.toString,
        "defaultUser" -> entity.defaultUser,
        "passPhrase" -> entity.passPhrase
        )

      val node = Neo4jRepository.saveEntity[KeyPairInfo](label, map)

      logger.debug(s"node - ${node.get}")
      node
    }

    override def fromGraph(nodeId: Long): KeyPairInfo = {
      val node = Neo4jRepository.findNodeById(nodeId)
      val map = Neo4jRepository.getProperties(node, "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")

      val keyPairInfo = KeyPairInfo (
        map.get("keyName").get.asInstanceOf[String],
        map.get("keyFingerprint").get.asInstanceOf[String],
        map.get("keyMaterial").get.asInstanceOf[String],
        map.get("filePath").get.asInstanceOf[String],
        KeyPairStatus.withName(map.get("status").get.asInstanceOf[String]),
        map.get("defaultUser").get.asInstanceOf[String],
        map.get("passPhrase").get.asInstanceOf[String]
      )

      logger.debug(s"Key pair info - ${keyPairInfo}")
      keyPairInfo
    }

  }

  implicit class RichUser(user: User) extends Neo4jRep[User] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "User"
    val has_publicKeys = "HAS_publicKeys"

    override def toGraph(entity: User): Option[Node] = {
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

      val node = Neo4jRepository.saveEntity[User](label, map)
      logger.debug(s"node - ${node.get}")

      //publicKeys: List[KeyPairInfo]
      //Relation HAS_publicKeys
      entity.publicKeys.foreach(publicKey => {
        val publicKeyNode = publicKey.toGraph(publicKey)
        Neo4jRepository.createRelation(has_publicKeys, node.get, publicKeyNode.get)
      })

      node
    }
    override def fromGraph(nodeId: Long): User = {
      val node = Neo4jRepository.findNodeById(nodeId)
      val map = Neo4jRepository.getProperties(node, "username", "password", "email", "uniqueId", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")

      // get public keys
      val keyPairInfos: scala.collection.mutable.ListBuffer[KeyPairInfo] = scala.collection.mutable.ListBuffer()

      val keyPairInfoNodes = Neo4jRepository.getNodesWithRelation(node, has_publicKeys)
      val keyPairInfo: KeyPairInfo = null
      keyPairInfoNodes.foreach(child => {
        logger.debug(s" child node ${child}")
        keyPairInfos += keyPairInfo.fromGraph(child.getId)
      })

      val user = User(map.get("username").get.asInstanceOf[String],
        map.get("password").get.asInstanceOf[String],
        map.get("email").get.asInstanceOf[String],
        map.get("uniqueId").get.asInstanceOf[String],
        keyPairInfos.toList,
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








