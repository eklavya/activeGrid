package com.imaginea.activegrid.core.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.imaginea.activegrid.core.models.KeyPairStatus.KeyPairStatus
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 26/9/16.
  */
case class KeyPairInfo(override val id: Option[Long]
                      , keyName: String
                      , keyFingerprint: String
                      , keyMaterial: String
                      , filePath: String
                      , status: KeyPairStatus
                      , defaultUser: String
                      , passPhrase: String
                      ) extends BaseEntity



object KeyPairInfo {

  implicit class RichKeyPairInfo(keyPairInfo: KeyPairInfo) extends Neo4jRep[KeyPairInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "KeyPairInfo"

    override def toNeo4jGraph(entity: KeyPairInfo): Option[Node] = {
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

    override def fromNeo4jGraph(nodeId: Long): KeyPairInfo = {
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


}