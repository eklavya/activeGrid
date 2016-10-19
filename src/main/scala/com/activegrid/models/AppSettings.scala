package com.activegrid.models

import org.neo4j.graphdb.{Node, NotFoundException, RelationshipType}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable


/**
  * Created by nagulmeeras on 27/09/16.
  */

case class AppSettings(override val id: Option[Long], settings: Map[String, String], authSettings: Map[String, String]) extends BaseEntity


object AppSettings {
  val repo = Neo4JRepository
  val labelName = "AppSettings"
  val settingsLableName = "Settings"
  val authSettingsLableName = "AuthSettings"
  val settingsRelationName = "Has_Settings"
  val authSettingsRelationName = "Has_AuthSettings"
  val logger = LoggerFactory.getLogger(getClass)

  implicit class AppSettingsImpl(entity: AppSettings) extends Neo4jRep[AppSettings] {

    override def toNeo4jGraph(): Option[Node] = {
      logger.info(s"Executing $getClass :: toNeo4jGraph")
      repo.withTx { neo =>
        val node: Node = repo.createNode(labelName)(neo)
        if (entity.settings != null) {
          logger.info(s"Persisting settings with relation ${entity.settings} ")
          val settingsNode = repo.createNode(settingsLableName)(neo)
          entity.settings.foreach { case (key, value) => settingsNode.setProperty(key, value) }
          createRelationShip(node, settingsNode, settingsRelationName)
        }
        if (entity.authSettings != null) {
          logger.info("Persisting auth settings with relation")
          val authSettingsNode = repo.createNode(authSettingsLableName)(neo)
          entity.authSettings.foreach { case (key, value) => authSettingsNode.setProperty(key, value) }
          createRelationShip(node, authSettingsNode, authSettingsRelationName)
        }
        Some(node)
      }
    }

    override def fromNeo4jGraph(nodeId: Option[Long]): Option[AppSettings] = {
      logger.info(s"Executing $getClass ::fromNeo4jGraph ")
      val appSettings = AppSettings.fromNeo4jGraph(nodeId)
      appSettings
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

  def fromNeo4jGraph(nodeId: Option[Long]): Option[AppSettings] = {

    repo.withTx {
      neo =>
        nodeId match {
          case Some(nodeId) => {
            var appSettings: AppSettings = AppSettings(Some(0), Map.empty, Map.empty)
            try {
              val node = repo.getNodeById(nodeId)(neo)
              val relationships = node.getRelationships
              val relationshipItr = relationships.iterator()
              val settings: mutable.Map[String, String] = mutable.Map.empty[String, String]
              val authSettings: mutable.Map[String, String] = mutable.Map.empty[String, String]
              while (relationshipItr.hasNext) {
                val relationship = relationshipItr.next()
                val endNode = relationship.getEndNode
                relationship.getType.name match {
                  case `settingsRelationName` => endNode.getAllProperties.map { case (key, value) => settings.put(key, value.toString) }
                  case `authSettingsRelationName` => endNode.getAllProperties.map { case (key, value) => settings.put(key, value.toString) }
                  case _ => None
                }
                appSettings = new AppSettings(Option.apply(node.getId), settings.toMap, authSettings.toMap)
              }
              Some(appSettings)
            } catch {
              case nfe: NotFoundException => None
              case exception: Exception => throw new Exception("Unable to save the entity")
            }
          }
          case None => None
        }
    }
  }

  def updateAppSettings(settings: Map[String, String], relation: String): Unit = {
    logger.info(s"Executing the $getClass :: updateAppSettings")
    try {
      val node = getAppSettingNode()
      node match {
        case Some(node) =>
          repo.withTx {
            neo =>
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
        case None => None
      }
    } catch {
      case exception: Exception => throw exception
    }
  }

  def deleteSettings(settingNames: List[String], relation: String): Boolean = {
    logger.info(s"Executing $getClass ::deleteSettings")
    try {
      val node = getAppSettingNode()
      node match {
        case Some(node) =>
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
              true
          }
        case None => false
      }
    } catch {
      case exception: Exception => throw new Exception("Unable to delete the settings")
    }
  }

  def getAppSettingNode(): Option[Node] = {
    repo.withTx {
      neo =>
        try {
          val nodes = repo.getAllNodesWithLabel(labelName)(neo)
          Some(nodes.head)
        } catch {
          case nse: NoSuchElementException => None
          case exception: Exception => throw new Exception(s"Unable to get nodes with label $labelName")
        }
    }
  }
}

