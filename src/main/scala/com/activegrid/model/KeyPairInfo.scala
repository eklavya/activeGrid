package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import com.activegrid.model.KeyPairStatus.KeyPairStatus
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */
case class KeyPairInfo(override val id: Option[Long],keyName: String,keyFingerprint: String,keyMaterial: String, filePath:String, status: KeyPairStatus, defaultUser: String, passPhrase: String )  extends BaseEntity

object KeyPairInfo{
  implicit class KeyPairInfoImpl(keyPairInfo: KeyPairInfo) extends Neo4jRep[KeyPairInfo] {

    val label = "KeyPairInfo"

    override def toNeo4jGraph(entity: KeyPairInfo): Option[Node] = {

      val map: Map[String, Any] = Map(
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

    override def fromNeo4jGraph(nodeId: Long): Option[KeyPairInfo] = {

      val listOfKeys: List[String] = List("keyName", "keyFingerprint", "keyMaterial", "filePath", "status", "defaultUser", "passPhrase")
      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)

      val keyName: String = propertyValues.get("keyName").get.toString
      val keyFingerprint: String = propertyValues.get("keyFingerprint").get.toString
      val keyMaterial: String = propertyValues.get("keyMaterial").get.toString
      val filePath: String = propertyValues.get("filePath").get.toString
      val status: KeyPairStatus =  KeyPairStatus.withName(propertyValues.get("status").get.asInstanceOf[String])
      val defaultUser: String = propertyValues.get("defaultUser").get.toString
      val passPhrase: String = propertyValues.get("passPhrase").get.toString


      Some(KeyPairInfo(Some(nodeId), keyName,keyFingerprint,keyMaterial, filePath, status, defaultUser, passPhrase))
    }

  }


}
