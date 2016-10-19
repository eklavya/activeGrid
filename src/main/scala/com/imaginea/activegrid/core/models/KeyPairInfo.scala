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
                       , filePath: Option[String]
                       , status: KeyPairStatus
                       , defaultUser: Option[String]
                       , passPhrase: Option[String]
                      ) extends BaseEntity

object KeyPairInfo {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  def apply(keyName: String, keyMaterial: String, filePath: Option[String], status: KeyPairStatus): KeyPairInfo =
    new KeyPairInfo(None, keyName, None, keyMaterial, filePath, status, None, None)

  def fromNeo4jGraph(nodeId: Long): Option[KeyPairInfo] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")

        val keyPairInfo = KeyPairInfo(
          Some(node.getId),
          map("keyName").toString,
          ActiveGridUtils.getValueFromMapAs[String](map, "keyFingerprint"),
          map("keyMaterial").toString,
          ActiveGridUtils.getValueFromMapAs[String](map, "filePath"),
          KeyPairStatus.toKeyPairStatus(map("status").toString),
          ActiveGridUtils.getValueFromMapAs[String](map, "defaultUser"),
          ActiveGridUtils.getValueFromMapAs[String](map, "passPhrase")
        )

        logger.debug(s"Key pair info - $keyPairInfo")
        Some(keyPairInfo)
      case None => None
    }
  }

  implicit class RichKeyPairInfo(keyPairInfo: KeyPairInfo) extends Neo4jRep[KeyPairInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "KeyPairInfo"

    override def toNeo4jGraph(entity: KeyPairInfo): Node = {
      logger.debug(s"toGraph for KeyPairInfo $entity")
      val map = Map(
        "keyName" -> entity.keyName,
        "keyFingerprint" -> entity.keyFingerprint.getOrElse(None),
        "keyMaterial" -> entity.keyMaterial,
        "filePath" -> entity.filePath.getOrElse(None),
        "status" -> entity.status.toString,
        "defaultUser" -> entity.defaultUser.getOrElse(None),
        "passPhrase" -> entity.passPhrase.getOrElse(None)
      )

      val node = Neo4jRepository.saveEntity[KeyPairInfo](label, entity.id, map)

      logger.debug(s"node - $node")
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[KeyPairInfo] = {
      KeyPairInfo.fromNeo4jGraph(nodeId)
    }

  }

}