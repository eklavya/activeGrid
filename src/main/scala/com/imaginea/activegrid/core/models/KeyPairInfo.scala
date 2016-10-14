package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by babjik on 26/9/16.
 */
case class KeyPairInfo(val id: Option[Long]
                       , val keyName: String
                       , val keyFingerprint: Option[String] = None
                       , val keyMaterial: Option[String] = None
                       , val filePath: Option[String] = None
                       , val status: KeyPairStatus
                       , val defaultUser: Option[String] = None
                       , val passPhrase: Option[String] = None
                        ) extends BaseEntity


object KeyPairInfo {
  import KeyPairStatus._

  implicit class RichKeyPairInfo(keyPairInfo: KeyPairInfo) extends Neo4jRep[KeyPairInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "KeyPairInfo"

    override def toNeo4jGraph(entity: KeyPairInfo): Option[Node] = {
      logger.debug(s"toGraph for KeyPairInfo ${entity}")
      val map: Map[String, Any] = Map(
        "keyName" -> entity.keyName,
        "keyFingerprint" -> entity.keyFingerprint.getOrElse(""),
        "keyMaterial" -> entity.keyMaterial.getOrElse(""),
        "filePath" -> entity.filePath.getOrElse(""),
        "status" -> entity.status.toString,
        "defaultUser" -> entity.defaultUser.getOrElse(""),
        "passPhrase" -> entity.passPhrase.getOrElse("")
      )

      val node = Neo4jRepository.saveEntity[KeyPairInfo](label, entity.id, map)

      logger.debug(s"node - ${node.get}")
      node
    }

    override def fromNeo4jGraph(nodeId: Long): KeyPairInfo = {
      val node = Neo4jRepository.findNodeById(nodeId).get
      val map = Neo4jRepository.getProperties(node, "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")

      val keyPairInfo = KeyPairInfo(
        Some(node.getId),
        map.get("keyName").get.asInstanceOf[String],
        Some(map.get("keyFingerprint").get.asInstanceOf[String]),
        Some(map.get("keyMaterial").get.asInstanceOf[String]),
        Some(map.get("filePath").get.asInstanceOf[String]),
        map.get("status").get.asInstanceOf[String],
        Some(map.get("defaultUser").get.asInstanceOf[String]),
        Some(map.get("passPhrase").get.asInstanceOf[String])
      )

      logger.debug(s"Key pair info - ${keyPairInfo}")
      keyPairInfo
    }

  }


}