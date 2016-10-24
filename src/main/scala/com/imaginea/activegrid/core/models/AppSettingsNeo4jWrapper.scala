package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by sivag on 6/10/16.
  */
object AppSettingsNeo4jWrapper {


  val labels: HashMap[String, String] = HashMap[String, String]("GS" -> "GeneralSettings", "AS" -> "AppSettings", "AUS" -> "AuthSettings", "HAS" -> "HAS_AUTH_SETTINGS", "HGS" -> "HAS_GENERAL_SETTINGS")
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val rep = Neo4jRepository


  def toNeo4jGraph(entity: ApplicationSettings): Node = {
    rep.withTx {
      neo => {
        val generalSettings = rep.createNode(labels("GS").toString)(neo)
        val authSettings = rep.createNode(labels("AUS").toString)(neo)
        setNodeProperties(generalSettings, entity.settings)
        setNodeProperties(authSettings, entity.authSettings)
        val appSettings = rep.createNode(labels("AS").toString)(neo)
        rep.createRelation(labels("HGS").toString, appSettings, generalSettings)
        rep.createRelation(labels("HAS").toString, appSettings, authSettings)
        appSettings
      }
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[ApplicationSettings] = {
    rep.withTx {
      neo => {
        rep.withTx { neo =>
          val settingNodes = rep.getAllNodesWithLabel(labels("AS").toString)(neo).toList
          settingNodes.map { node => ApplicationSettings(Some(node.getId), getSettingsByRelation(node, labels("HGS").toString), getSettingsByRelation(node, labels("HAS").toString)) }.headOption
        }
      }
    }
  }

  def updateSettings(settingsToUpdate: Map[String, String], settingsType: String): Future[ExecutionStatus] = {
    if (settingsType.equalsIgnoreCase("AUTH_SETTINGS"))
      updateOrDeleteSettings(settingsToUpdate, labels("HAS").toString, "UPDATE")
    else
      updateOrDeleteSettings(settingsToUpdate, labels("HGS").toString, "UPDATE")

  }

  def deleteSetting(settingsToDelete: Map[String, String], settingsType: String): Future[ExecutionStatus] = {
    if (settingsType.equalsIgnoreCase("AUTH_SETTINGS"))
      updateOrDeleteSettings(settingsToDelete, labels("HAS").toString, "DELETE")
    else
      updateOrDeleteSettings(settingsToDelete, labels("HGS").toString, "DELETE")
  }

  def getSettingsByRelation(rootNode: Node, relationName: String): Map[String, String] = {
    val relationNode = getRelationNodeByName(rootNode, relationName)
    relationNode match {
      case Some(node) => node.getAllProperties.mapValues(_.toString()).toMap[String, String]
      case None => Map.empty[String, String]
    }
  }

  def getRelationNodeByName(rootNode: Node, relationName: String): Option[Node] = {
    rootNode.getRelationships.find(relation => relation.getType.name() == relationName) match {
      case Some(relationship) => Some(relationship.getEndNode)
      case None => None
    }
  }

  def updateOrDeleteSettings(settings: Map[String, String], relationName: String, updateOrDelete: String): Future[ExecutionStatus] = Future {
    rep.withTx {
      neo => {
        rep.withTx { neo =>
          val settingNodes = rep.getAllNodesWithLabel(labels("AS").toString)(neo).headOption
          settingNodes match {
            case Some(rootNode) => val relationNode = getRelationNodeByName(rootNode, relationName)
              relationNode match {
                case Some(dbnode) =>
                  val todelete = if (updateOrDelete.equalsIgnoreCase("DELETE")) true else false
                  settings.foreach {
                    case (k, v) =>
                      if (todelete)
                        dbnode.removeProperty(k)
                      else
                        dbnode.setProperty(k, v.toString)
                  }
                case None => ExecutionStatus(false)
              }
            case None => ExecutionStatus(false)
          }
        }
      }
    }
    ExecutionStatus(true)
  }

  def setNodeProperties(n: Node, settings: Map[String, String]) {
    settings.foreach {
      case (k, v) => n.setProperty(k, v);
    }
  }
}

