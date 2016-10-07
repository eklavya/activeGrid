package com.activegrid.models

import org.neo4j.graphdb.{Node, RelationshipType}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable


/**
  * Created by nagulmeeras on 27/09/16.
  */

case class AppSettings(override val id: Option[Long], settings: Map[String, String], authSettings: Map[String, String]) extends BaseEntity


object AppSettings {
  val repo = Neo4JRepo

  implicit class AppSettingsImpl(entity: AppSettings) extends Neo4jRep[AppSettings] {

    val labelName = "AppSettings"
    val settingsLableName = "Settings"
    val authSettingsLableName = "AuthSettings"
    val settingsRelationName = "Has_Settings"
    val authSettingsRelationName = "Has_AuthSettings"

    val logger = LoggerFactory.getLogger(getClass)

    override def toNeo4jGraph(): Node = {

      repo.withTx { neo =>
        val node: Node = repo.createNode(labelName)(neo)
        if (entity.settings != null) {
          val settingsNode = repo.createNode(settingsLableName)(neo)
          entity.settings.foreach { case (key, value) => settingsNode.setProperty(key, value) }
          createRelationShip(node, settingsNode, settingsRelationName)
        }
        if (entity.authSettings != null) {
          val authSettingsNode = repo.createNode(authSettingsLableName)(neo)
          entity.authSettings.foreach { case (key, value) => authSettingsNode.setProperty(key, value) }
          createRelationShip(node, authSettingsNode, authSettingsRelationName)
        }
        node
      }
    }

    override def fromNeo4jGraph(): AppSettings = {
      var appSettings: AppSettings = null
      repo.withTx {
        neo =>

          val nodes = repo.getAllNodesWithLabel("AppSettings")(neo)

          nodes.foreach {
            node =>
              val relationships = node.getRelationships
              val relationshipItr = relationships.iterator()
              val settings: mutable.Map[String, String] = mutable.Map()
              val authSettings: mutable.Map[String, String] = mutable.Map()
              while (relationshipItr.hasNext) {
                val relationship = relationshipItr.next()
                val endNode = relationship.getEndNode
                relationship.getType.name match {
                  case `settingsRelationName` => endNode.getAllProperties.map { case (key, value) => settings.put(key, value.toString) }
                  case `authSettingsRelationName` => endNode.getAllProperties.map { case (key, value) => settings.put(key, value.toString) }
                  case _ => None
                }
              }

              appSettings = new AppSettings(Option.apply(node.getId), settings.toMap, authSettings.toMap)

          }
      }

      appSettings
    }


    def getAppSettingNode(): Node = {
      repo.withTx {
        neo => val nodes = repo.getAllNodesWithLabel(labelName)(neo)
          nodes.head
      }
    }

    def updateAppSettings(settings: Map[String, String], relation: String): Unit = {
      val node = getAppSettingNode()
      repo.withTx {
        neo =>
          val relationships = node.getRelationships
          val itr = relationships.iterator()
          while (itr.hasNext) {
            val relationship = itr.next()
            val endNode = relationship.getEndNode
            logger.info("nodes relation :" + relationship.getType.name() + " actual relation :" + relation)
            if (relationship.getType.name.equalsIgnoreCase(relation)) {
              logger.info("COming to add new property")
              settings.foreach { case (key, value) => endNode.setProperty(key, value) }
            }
          }
      }
    }

    def deleteSettings(settingNames: List[String], relation: String): Unit = {
      val node = getAppSettingNode()
      repo.withTx {
        neo =>
          val relationships = node.getRelationships
          val itr = relationships.iterator()
          while (itr.hasNext) {
            val relationship = itr.next()
            val endNode = relationship.getEndNode
            logger.info("nodes relation :" + relationship.getType.name() + " actual relation :" + relation)
            if (relationship.getType.name.equalsIgnoreCase(relation)) {
              logger.info("Coming to delete property")
              settingNames.foreach(entry => endNode.removeProperty(entry))
            }
          }
      }
    }

  }

  implicit def createRelationShip(parentNode: Node, childNode: Node, relationship: String): Unit = {

    repo.withTx {
      neo =>
        parentNode.createRelationshipTo(childNode, new RelationshipType {
          override def name(): String = relationship
        })
    }
  }

}
