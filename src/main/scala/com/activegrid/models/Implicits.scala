package com.activegrid.models

import org.neo4j.graphdb.{Node, RelationshipType}
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * Created by nagulmeeras on 05/10/16.
  */
object Implicits {
  val repo = Neo4JRepo

  implicit class AppSettingsImpl(entity: AppSettings) extends Neo4jRep[AppSettings] {
    val labelName = "AppSettings"
    val settingsLableName = "Settings"
    val authSettingsLableName = "AuthSettings"
    val settingsRelationName = "Has_Settings"
    val authSettingsRelationName = "Has_AuthSettings"

    val logger  = LoggerFactory.getLogger(getClass)
    override def toGraph(): Node = {

      repo.withTx { neo =>
        val node: Node = repo.createNode(labelName)(neo)
        if (entity.settings != null){
          val settingsNode = repo.createNode(settingsLableName)(neo)
          entity.settings.foreach (prop => settingsNode.setProperty(prop._1, prop._2))
          createRelationShip(node, settingsNode, settingsRelationName)
        }
        if (entity.authSettings != null) {
          val authSettingsNode = repo.createNode(authSettingsLableName)(neo)
          entity.authSettings.foreach(prop => authSettingsNode.setProperty(prop._1, prop._2))
          createRelationShip(node, authSettingsNode, authSettingsRelationName)
        }
        node
      }
    }

    override def fromGraph(): AppSettings = {
      var appSettings: AppSettings = null
      repo.withTx {
        neo =>

          val nodes = repo.getAllNodesWithLabel("AppSettings")(neo)

          nodes.foreach {
            node =>
              val relationships = node.getRelationships
              val relationshipItr = relationships.iterator()
              var settings: mutable.Map[String, String] = null
              var authSettings: mutable.Map[String, String] = null
              while (relationshipItr.hasNext) {
                val relationship = relationshipItr.next()
                val endNode = relationship.getEndNode
                println(relationship.getType.name())
                relationship.getType.name match {
                  case `settingsRelationName` => settings = convertToMap(endNode.getAllProperties)
                  case `authSettingsRelationName` => authSettings = convertToMap(endNode.getAllProperties)
                  case _ => None
                }
              }

              appSettings = new AppSettings( Option.apply(node.getId) , if(settings != null) settings.toMap else Map(), if (authSettings != null) authSettings.toMap else Map())

          }
      }

      appSettings
    }

    def convertToMap(from: java.util.Map[String, Object]): mutable.Map[String, String] = {
      logger.info(s"Before conversion of map : $from")
      val toMap: mutable.Map[String, String] = mutable.Map()
      val itr = from.entrySet().iterator()
      while (itr.hasNext) {
        val obj = itr.next()
        toMap.put(obj.getKey, obj.getValue.toString)
      }
      logger.info(s" After conversion of map : $toMap")
      toMap
    }

    def getAppSettingNode(): Node = {
      repo.withTx {
        neo => val nodes = repo.getAllNodesWithLabel(labelName)(neo)
          nodes.head
      }
    }

    def updateAppSettings(settings : Map[String,String], relation: String ): Unit = {
      val node = getAppSettingNode()
      repo.withTx {
        neo =>
          val relationships = node.getRelationships
          val itr = relationships.iterator()
          while (itr.hasNext) {
            val relationship = itr.next()
            val endNode = relationship.getEndNode
            logger.info("nodes relation :"+relationship.getType.name()+" actual relation :"+relation)
            if (relationship.getType.name.equalsIgnoreCase(relation)) {
              logger.info("COming to add new property")
                settings.foreach(entry => endNode.setProperty(entry._1, entry._2))
            }
          }
      }
    }
    def deleteSettings(settingNames: List[String] , relation : String) : Unit = {
      val node = getAppSettingNode()
      repo.withTx {
        neo =>
          val relationships = node.getRelationships
          val itr = relationships.iterator()
          while (itr.hasNext) {
            val relationship = itr.next()
            val endNode = relationship.getEndNode
            logger.info("nodes relation :"+relationship.getType.name()+" actual relation :"+relation)
            if (relationship.getType.name.equalsIgnoreCase(relation)) {
              logger.info("Coming to delete property")
              settingNames.foreach(entry => endNode.removeProperty(entry))
            }
          }
      }
    }

  }

  implicit class SettingsImpl(setting: Setting) extends Neo4jRep[Setting] {
    val labelName = "Setting"

    override def toGraph(): Node = {
      repo.withTx {
        neo =>
          val node = repo.createNode(labelName)(neo)
          node.setProperty("key", setting.key)
          node.setProperty("value", setting.value)
          node
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
