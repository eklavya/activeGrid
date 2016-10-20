package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
 * Created by babjik on 26/9/16.
 */

case class KeyPairInfo(override val id: Option[Long]
                       , val keyName: String
                       , val keyFingerprint: Option[String] = None
                       , val keyMaterial: Option[String] = None
                       , val filePath: Option[String] = None
                       , val status: KeyPairStatus
                       , val defaultUser: Option[String] = None
                       , val passPhrase: Option[String] = None
                        ) extends BaseEntity {

  def this(keyName: String, keyMaterial: String, filePath: String, status: KeyPairStatus) =
    this(id = None
      , keyName = keyName
      , keyMaterial = Some(keyMaterial)
      , filePath = Some(filePath)
      , status = NotYetUploadedKeyPair
    )
}

object KeyPairInfo {

  import KeyPairStatus._

  def apply(keyName: String, keyMaterial: String, filePath: String, status: KeyPairStatus): KeyPairInfo =
    new KeyPairInfo(keyName, keyMaterial, filePath, status)

  implicit class RichKeyPairInfo(keyPairInfo: KeyPairInfo) extends Neo4jRep[KeyPairInfo] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "KeyPairInfo"

    override def toNeo4jGraph(entity: KeyPairInfo): Node = {
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

      logger.debug(s"node - ${node}")
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[KeyPairInfo] = {
      val node = Neo4jRepository.findNodeById(nodeId)
      val mapOption = Neo4jRepository.getProperties(node, "keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
      mapOption.map( map => {
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
      }).orElse(None)
    }
  }

}