/*
 * Copyright (c) 1999-2013 Pramati Technologies Pvt Ltd. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Pramati Technologies.
 * You shall not disclose such Confidential Information and shall use it only in accordance with
 * the terms of the source code license agreement you entered into with Pramati Technologies.
 */
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

  val labels: HashMap[String, String] =
    HashMap[String, String](
      "GS" -> "GeneralSettings",
      "AS" -> "AppSettings",
      "AUS" -> "AuthSettings",
      "HAS" -> "HAS_AUTH_SETTINGS",
      "HGS" -> "HAS_GENERAL_SETTINGS"
    )

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
          settingNodes.map { node => ApplicationSettings(Some(node.getId)
            , getSettingsByRelation(node, labels("HGS").toString)
            , getSettingsByRelation(node, labels("HAS").toString))
          }.headOption
        }
      }
    }
  }

  private def label(settingsType: String): String =
    if (settingsType.equalsIgnoreCase("AUTH_SETTINGS")) labels("HAS") else labels("HGS")

  def updateSettings(settingsToUpdate: Map[String, String], settingsType: String): Future[ExecutionStatus] = {
    updateOrDeleteSettings(settingsToUpdate, label(settingsType), "UPDATE")
  }

  def deleteSetting(settingsToDelete: Map[String, String], settingsType: String): Future[ExecutionStatus] = {
    updateOrDeleteSettings(settingsToDelete, label(settingsType), "DELETE")
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

  def updateOrDeleteSettings(settings: Map[String, String]
                             , relationName: String
                             , updateOrDelete: String): Future[ExecutionStatus] = Future {
    rep.withTx { neo =>
      val settingNodes = rep.getAllNodesWithLabel(labels("AS").toString)(neo).headOption
      settingNodes.flatMap(rootNode => {
        val relationNode = getRelationNodeByName(rootNode, relationName)
        relationNode.map { dbNode =>
          val toDelete = updateOrDelete.equalsIgnoreCase("DELETE")
          settings.foreach {
            case (k, v) if (toDelete) => dbNode.removeProperty(k)
            case (k, v) => dbNode.setProperty(k, v.toString)
          }
          ExecutionStatus(true)
        }
      }).getOrElse(ExecutionStatus(false))
    }
  }

  def setNodeProperties(n: Node, settings: Map[String, String]) {
    settings.foreach {
      case (k, v) => n.setProperty(k, v);
    }
  }
}

