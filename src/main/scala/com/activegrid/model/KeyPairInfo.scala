package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */
case class KeyPairInfo(override val id: Option[Long],keyName: String,keyFingerprint: String,keyMaterial: String, filePath:String, status: KeyPairStatus, defaultUser: String, passPhrase: String )  extends BaseEntity

object KeyPairInfo{

  def fromNeo4jGraph(id: Option[Long]): Option[KeyPairInfo] = {
    id match {
      case Some(nodeId) =>
        val listOfKeys = List("keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
        val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
        val keyName = propertyValues("keyName").toString
        val keyFingerprint = propertyValues("keyFingerprint").toString
        val keyMaterial = propertyValues("keyMaterial").toString
        val filePath = propertyValues("filePath").toString
        //val status: KeyPairStatus =  KeyPairStatus.withName(propertyValues.get("status").get.asInstanceOf[String])
        val status: KeyPairStatus = KeyPairStatus.toKeyPairStatus(propertyValues("status").asInstanceOf[String])
        val defaultUser = propertyValues("defaultUser").toString
        val passPhrase = propertyValues("passPhrase").toString

        Some(KeyPairInfo(Some(nodeId), keyName, keyFingerprint, keyMaterial, filePath, status, defaultUser, passPhrase))

      case None => None
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

    override def fromNeo4jGraph(id: Option[Long]): Option[KeyPairInfo] = {
     KeyPairInfo.fromNeo4jGraph(id)
    }

  }

}

