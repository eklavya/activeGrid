package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */
case class KeyPairInfo(override val id: Option[Long], keyName: String, keyFingerprint: String, keyMaterial: String, filePath: String, status: KeyPairStatus, defaultUser: String, passPhrase: String) extends BaseEntity

object KeyPairInfo {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[KeyPairInfo] = {
    val listOfKeys = List("keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
    val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
    if (propertyValues.nonEmpty) {
      val keyName = propertyValues("keyName").toString
      val keyFingerprint = propertyValues("keyFingerprint").toString
      val keyMaterial = propertyValues("keyMaterial").toString
      val filePath = propertyValues("filePath").toString
      val status: KeyPairStatus = KeyPairStatus.toKeyPairStatus(propertyValues("status").asInstanceOf[String])
      val defaultUser = propertyValues("defaultUser").toString
      val passPhrase = propertyValues("passPhrase").toString

      Some(KeyPairInfo(Some(nodeId), keyName, keyFingerprint, keyMaterial, filePath, status, defaultUser, passPhrase))
    }
    else {
      logger.warn(s"could not get graph properties for KeyPairInfo node with $nodeId")
      None
    }
  }

  implicit class KeyPairInfoImpl(keyPairInfo: KeyPairInfo) extends Neo4jRep[KeyPairInfo] {

    val label = "KeyPairInfo"

    override def toNeo4jGraph(entity: KeyPairInfo): Node = {
      val map = Map(
        "keyName" -> entity.keyName,
        "keyFingerprint" -> entity.keyFingerprint,
        "keyMaterial" -> entity.keyMaterial,
        "filePath" -> entity.filePath,
        "status" -> entity.status.toString,
        "defaultUser" -> entity.defaultUser,
        "passPhrase" -> entity.passPhrase)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[KeyPairInfo](label, map)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[KeyPairInfo] = {
      KeyPairInfo.fromNeo4jGraph(id)
    }
  }
}

