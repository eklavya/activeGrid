package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.{Node, Relationship}
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 3/10/16.
  */
object Implicits {

  implicit class RichImageInfo(imageInfo: ImageInfo) extends Neo4jRep[ImageInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ImageInfo"

    override def toGraph(entity: ImageInfo): Option[Node] = {
      logger.debug(s"toGraph for Image ${imageInfo}")
      // TODO: Image fields
      val map: Map[String, Any] = Map()
      val node = Neo4jRepository.saveEntity[ImageInfo](label, entity.id, map)

      logger.debug(s"node - ${node.get}")
      node
    }

    override def fromGraph(nodeId: Long): ImageInfo = {
      ImageInfo(Some(0L),"", "", "", false, "", "", "", "", "", "", "", "", "")
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

      val node = Neo4jRepository.saveEntity[KeyPairInfo](label, entity.id, map)

      logger.debug(s"node - ${node.get}")
      node
    }

    override def fromGraph(nodeId: Long): KeyPairInfo = {
      val node = Neo4jRepository.findNodeById(nodeId).get
      val map = Neo4jRepository.getProperties(node, "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")

      val keyPairInfo = KeyPairInfo (
        Some(node.getId),
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

      val node = Neo4jRepository.saveEntity[User](label, entity.id, map)
      logger.debug(s"node - ${node.get}")

      //publicKeys: List[KeyPairInfo]
      //Relation HAS_publicKeys
      val relations = entity.publicKeys.map(publicKey => addKeyPair(node.get.getId, publicKey))
      node
    }

    def addKeyPair(userId: Long, keyPairInfo: KeyPairInfo): Relationship = {
      val userNode = Neo4jRepository.findNodeById(userId).get
      val publicKeyNode = keyPairInfo.toGraph(keyPairInfo)
      Neo4jRepository.createRelation(has_publicKeys, userNode, publicKeyNode.get)
    }

    override def fromGraph(nodeId: Long): User = {
      val node = Neo4jRepository.findNodeById(nodeId).get
      val map = Neo4jRepository.getProperties(node, "username", "password", "email", "uniqueId", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "displayName")

      val keyPairInfoNodes = Neo4jRepository.getNodesWithRelation(node, has_publicKeys)
      val keyPairInfo: KeyPairInfo = null

      val keyPairInfos = keyPairInfoNodes.map(keyPairNode => {keyPairInfo.fromGraph(keyPairNode.getId)}).toList

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


  implicit class RichUserGroup(userGroup: UserGroup) extends Neo4jRep2[UserGroup] {
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
  implicit class RichResourceACL(resource: ResourceACL) extends Neo4jRep2[ResourceACL] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ResourceACL"

    override def toGraph(resource: ResourceACL): Option[Node] =
      Neo4jRepository.saveEntity[ResourceACL](resource,label)

    override def fromGraph(nodeId: Long): Option[ResourceACL] = Neo4jRepository.getEntity[ResourceACL](nodeId)
  }
 /* implicit class RichResourceACL(resourceACL: ResourceACL) extends Neo4jRep[ResourceACL] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ResourceACL"

    override def toGraph(entity: ResourceACL): Option[Node] = super.toGraph(entity)

    override def fromGraph(nodeId: Long): ResourceACL = super.fromGraph(nodeId)
  }*/

 /* implicit class RichUserGroup(userGroup: UserGroup) extends Neo4jRep[UserGroup] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "UserGroup"

    override def toGraph(entity: UserGroup): Option[Node] = {
      val map: Map[String, Any] = Map("name" -> entity.name)

      val node = Neo4jRepository.saveEntity[KeyPairInfo](label, entity.id, map)

      logger.debug(s"user group - ${node.get}")
      node
    }

    override def fromGraph(nodeId: Long): UserGroup = super.fromGraph(nodeId)
  }*/
}








