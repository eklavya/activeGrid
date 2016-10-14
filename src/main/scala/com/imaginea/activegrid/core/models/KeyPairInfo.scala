package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 26/9/16.
  */
case class KeyPairInfo(override val id: Option[Long]
                       , keyName: String
                       , keyFingerprint: Option[String]
                       , keyMaterial: String
                       , filePath: String
                       , status: KeyPairStatus
                       , defaultUser: Option[String]
                       , passPhrase: Option[String]
                      ) extends BaseEntity {
  def this(keyName: String, keyMaterial: String, filePath: String, status: KeyPairStatus)
  = this(None, keyName, None, keyMaterial, filePath, status, None, None)
}


object KeyPairInfo {

  def apply(keyName: String, keyMaterial: String, filePath: String, status: KeyPairStatus): KeyPairInfo =
    new KeyPairInfo(None, keyName, None, keyMaterial, filePath, status, None, None)

  implicit class RichKeyPairInfo(keyPairInfo: KeyPairInfo) extends Neo4jRep[KeyPairInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "KeyPairInfo"

    override def toNeo4jGraph(entity: KeyPairInfo): Option[Node] = {
      logger.debug(s"toGraph for KeyPairInfo ${entity}")
      val map = Map(
        "keyName" -> entity.keyName,
        "keyFingerprint" -> entity.keyFingerprint.getOrElse(None),
        "keyMaterial" -> entity.keyMaterial,
        "filePath" -> entity.filePath,
        "status" -> entity.status.toString,
        "defaultUser" -> entity.defaultUser.getOrElse(None),
        "passPhrase" -> entity.passPhrase.getOrElse(None)
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
        ActiveGridUtils.getValueFromMapAs[String](map, "keyFingerprint"),
        map.get("keyMaterial").get.asInstanceOf[String],
        map.get("filePath").get.asInstanceOf[String],
        KeyPairStatus.toKeyPairStatus(map.get("status").get.asInstanceOf[String]),
        ActiveGridUtils.getValueFromMapAs[String](map, "defaultUser"),
        ActiveGridUtils.getValueFromMapAs[String](map, "passPhrase")
      )

      logger.debug(s"Key pair info - ${keyPairInfo}")
      keyPairInfo
    }

  }

}