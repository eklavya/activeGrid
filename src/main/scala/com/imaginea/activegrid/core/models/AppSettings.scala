package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.{Node, NotFoundException, RelationshipType}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._


/**
  * Created by nagulmeeras on 27/09/16.
  */

case class AppSettings(override val id: Option[Long], settings: Map[String, String], authSettings: Map[String, String]) extends BaseEntity

object AppSettings {
  val repo = Neo4jRepository
  val labelName = "AppSettings"
  val settingsLableName = "Settings"
  val authSettingsLableName = "AuthSettings"
  val settingsRelationName = "Has_Settings"
  val authSettingsRelationName = "Has_AuthSettings"
  val logger = LoggerFactory.getLogger(getClass)

  implicit class AppSettingsImpl(entity: AppSettings) extends Neo4jRep[AppSettings] {

    override def toNeo4jGraph(entity: AppSettings): Node = {
      logger.info(s"Executing $getClass :: toNeo4jGraph")
      repo.withTx { neo =>
        val node: Node = repo.createNode(labelName)(neo)
        logger.info(s"Persisting settings with relation ${entity.settings} ")
        val settingsNode = repo.createNode(settingsLableName)(neo)
        entity.settings.foreach { case (key, value) => settingsNode.setProperty(key, value) }
        createRelationShip(node, settingsNode, settingsRelationName)
        logger.info("Persisting auth settings with relation")
        val authSettingsNode = repo.createNode(authSettingsLableName)(neo)
        entity.authSettings.foreach { case (key, value) => authSettingsNode.setProperty(key, value) }
        createRelationShip(node, authSettingsNode, authSettingsRelationName)
        node
      }
    }

    override def fromNeo4jGraph(nodeId: Long): Option[AppSettings] = {
      logger.info(s"Executing $getClass ::fromNeo4jGraph ")
      val appSettings = AppSettings.fromNeo4jGraph(nodeId)
      appSettings
    }
  }

  implicit def createRelationShip(parentNode: Node, childNode: Node, relationship: String): Unit = {
    logger.info(s"Executing $getClass :: createRelationShip")
    repo.withTx {
      neo =>
        parentNode.createRelationshipTo(childNode, new RelationshipType {
          override def name(): String = relationship
        })
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[AppSettings] = {
    repo.withTx {
      neo =>
        try {
          val node = repo.getNodeById(nodeId)(neo)
          if (repo.hasLabel(node, labelName)) {
            val settingsMap = collection.mutable.Map.empty[String, Map[String, String]]
            node.getRelationships.map(relationship => {
              val endNode = relationship.getEndNode
              val map = endNode.getAllProperties.foldLeft(Map[String, String]())((map, prop) => map + ((prop._1, prop._2.asInstanceOf[String])))
              relationship.getType.name match {
                case `settingsRelationName` => settingsMap.put("settings", map)
                case `authSettingsRelationName` => settingsMap.put("authSettings", map)
                case _ => None
              }
            })
            Some(new AppSettings(Option.apply(node.getId), settingsMap("settings"), settingsMap("authSettings")))
          } else {
            logger.warn(s"Node is not found with ID:$nodeId and Label : $labelName")
            None
          }
        } catch {
          case nfe: NotFoundException =>
            logger.warn(nfe.getMessage, nfe)
            None
        }
    }
  }

  def updateAppSettings(settings: Map[String, String], relation: String): Unit = {
    logger.info(s"Executing the $getClass :: updateAppSettings")
    repo.withTx {
      neo =>
        getAppSettingNode.foreach {
          node =>
            val relationships = node.getRelationships
            val itr = relationships.iterator()
            while (itr.hasNext) {
              val relationship = itr.next()
              val endNode = relationship.getEndNode
              logger.info("nodes relation :" + relationship.getType.name() + " actual relation :" + relation)
              if (relationship.getType.name.equalsIgnoreCase(relation)) {
                logger.info("Coming to add new property")
                settings.foreach { case (key, value) => endNode.setProperty(key, value) }
              }
            }
        }
    }
  }

  def deleteSettings(settingNames: List[String], relation: String): Boolean = {
    logger.info(s"Executing $getClass ::deleteSettings")
    repo.withTx {
      neo =>
        getAppSettingNode.foreach {
          node =>
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
        true
    }
  }

  def getAppSettingNode: Option[Node] = {
    repo.withTx {
      neo =>
        try {
          val nodes = repo.getAllNodesWithLabel(labelName)(neo)
          nodes.headOption
        } catch {
          case nse: NoSuchElementException =>
            logger.warn(nse.getMessage, nse)
            None
        }
    }
  }
}

