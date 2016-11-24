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

  val labels =
    HashMap(
      "GS" -> "GeneralSettings",
      "AS" -> "AppSettings",
      "AUS" -> "AuthSettings",
      "HAS" -> "HAS_AUTH_SETTINGS",
      "HGS" -> "HAS_GENERAL_SETTINGS")

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val rep = Neo4jRepository

  def toNeo4jGraph(entity: ApplicationSettings): Node = {
    rep.withTx {
      neo => {
        val generalSettings = rep.createNode(labels("GS"))(neo)
        val authSettings = rep.createNode(labels("AUS"))(neo)
        setNodeProperties(generalSettings, entity.settings)
        setNodeProperties(authSettings, entity.authSettings)
        val appSettings = rep.createNode(labels("AS"))(neo)
        rep.createRelation(labels("HGS"), appSettings, generalSettings)
        rep.createRelation(labels("HAS"), appSettings, authSettings)
        appSettings
      }
    }
  }

  def setNodeProperties(n: Node, settings: Map[String, String]) {
    settings.foreach {
      case (k, v) => n.setProperty(k, v);
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[ApplicationSettings] = {
    rep.withTx {
      neo => {
        rep.withTx { neo =>
          val settingNodes = rep.getAllNodesWithLabel(labels("AS"))(neo).toList
          settingNodes.map { node => ApplicationSettings(Some(node.getId)
            , getSettingsByRelation(node, labels("HGS"))
            , getSettingsByRelation(node, labels("HAS")))
          }.headOption
        }
      }
    }
  }

  def getSettingsByRelation(rootNode: Node, relationName: String): Map[String, String] = {
    val relationNode = getRelationNodeByName(rootNode, relationName)
    relationNode match {
      case Some(node) => node.getAllProperties.mapValues(_.toString()).toMap[String, String]
      case None => Map.empty[String, String]
    }
  }

  def updateSettings(settingsToUpdate: Map[String, String], settingsType: String): Future[ExecutionStatus] = {
    updateOrDeleteSettings(settingsToUpdate, label(settingsType), "UPDATE")
  }

  def deleteSetting(settingsToDelete: Map[String, String], settingsType: String): Future[ExecutionStatus] = {
    updateOrDeleteSettings(settingsToDelete, label(settingsType), "DELETE")
  }

  private def label(settingsType: String) =
    if (settingsType.equalsIgnoreCase("AUTH_SETTINGS")) labels("HAS") else labels("HGS")

  def updateOrDeleteSettings(settings: Map[String, String]
                             , relationName: String
                             , updateOrDelete: String): Future[ExecutionStatus] = Future {
    rep.withTx { neo =>
      val settingNodes = rep.getAllNodesWithLabel(labels("AS"))(neo).headOption
      settingNodes.flatMap(rootNode => {
        val relationNode = getRelationNodeByName(rootNode, relationName)
        relationNode.map { dbNode =>
          val toDelete = updateOrDelete.equalsIgnoreCase("DELETE")
          settings.foreach {
            case (k, v) => if (toDelete) dbNode.removeProperty(k) else dbNode.setProperty(k, v.toString)
          }
          ExecutionStatus(true)
        }
      }).getOrElse(ExecutionStatus(false))
    }
  }

  def getRelationNodeByName(rootNode: Node, relationName: String): Option[Node] = {
    rootNode.getRelationships.find(relation => relation.getType.name() == relationName) match {
      case Some(relationship) => Some(relationship.getEndNode)
      case None => None
    }
  }
}

