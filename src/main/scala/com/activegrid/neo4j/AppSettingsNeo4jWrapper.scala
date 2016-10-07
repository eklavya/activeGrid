package com.activegrid.neo4j

import com.activegrid.entities.AppSettings
import com.activegrid.utils.Utils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Relationship
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.immutable.HashMap

/**
  * Created by sivag on 6/10/16.
  */
class AppSettingsNeo4jWrapper extends Neo4JRepo[AppSettings] with DBWrapper {

  val lables: HashMap[String, String] = HashMap[String, String]("GS" -> "GeneralSettings", "AS" -> "AppSettings", "AUS" -> "AuthSettings", "HAS" -> "HAS_AUTH_SETTINGS", "HGS" -> "HAS_GENERAL_SETTINGS")
  val util = new Utils();
  val logger = Logger(LoggerFactory.getLogger(getClass.getName));


  override def toGraph(entity: AppSettings): Unit = {
    withTx {
      neo => {
        val generalSettings = createNode(lables.get("GS").toString)(neo)
        val authSettings = createNode(lables.get("AUS").toString)(neo)
        util.setNodeProperties(generalSettings, entity.settings)
        util.setNodeProperties(authSettings, entity.authSettings)
        val appSettings = createNode(lables.get("AS").toString)(neo)
        appSettings --> lables.get("HGS").toString --> generalSettings
        appSettings --> lables.get("HAS").toString --> authSettings

      }
    }

  }

  override def fromGraph(nodeId: Long): AppSettings = {
    withTx {
      neo => {
        var genaralSettings = Map.empty[String, String];
        var authSettings = Map.empty[String, String]
        withTx { neo =>
          val settingNodes = getAllNodesWithLabel(lables.get("AS").toString)(neo)
          for (n <- settingNodes) {
            logger.info(n.getAllProperties.toString)
            val iterator = n.getRelationships().iterator()
            while (iterator.hasNext) {
              val relation: Relationship = iterator.next
              logger.info("Processing " + relation.getType.name())
              if (relation.getType.name.equalsIgnoreCase(lables.get("HAS").toString)) {
                logger.info("Processing " + relation.getType.name())
                authSettings = relation.getEndNode.getAllProperties.asScala.mapValues(_.asInstanceOf[String]).toMap
              }
              if (relation.getType.name.equalsIgnoreCase(lables.get("HGS").toString)) {
                logger.info("Processing " + relation.getType.name())
                genaralSettings = relation.getEndNode.getAllProperties.asScala.mapValues(_.asInstanceOf[String]).toMap
              }
            }
          }
          AppSettings(genaralSettings, authSettings);
        }
      }
    }
  }

  def updateSettings(settingsMap: Map[String, String], relationName: String): String = {
    withTx {
      neo => {
        var genaralSettings = Map.empty[String, String];
        withTx { neo =>
          val settingNodes = getAllNodesWithLabel(lables.get("AS").toString)(neo)
          for (n <- settingNodes) {
            logger.info(n.getAllProperties.toString)
            val iterator = n.getRelationships().iterator()
            while (iterator.hasNext) {
              val relation: Relationship = iterator.next
              logger.info("Processing " + relation.getType.name())
              if (relation.getType.name.equalsIgnoreCase(relationName)) {
                val node = relation.getEndNode;
                settingsMap.foreach {
                  case (k, v) => node.setProperty(k, v.toString)
                }
              }
            }
          }
        }
        "success"
      }
    }
  }

  def deleteSetting(settingsMap: Map[String, String], relationName: String): String = {
    withTx {
      neo => {
        var genaralSettings = Map.empty[String, String];
        withTx { neo =>
          val settingNodes = getAllNodesWithLabel(lables.get("AS").toString)(neo)
          for (n <- settingNodes) {
            logger.info(n.getAllProperties.toString)
            val iterator = n.getRelationships().iterator()
            while (iterator.hasNext) {
              val relation: Relationship = iterator.next
              logger.info("Processing " + relation.getType.name())
              if (relation.getType.name.equalsIgnoreCase(relationName)) {
                val node = relation.getEndNode;
                settingsMap.foreach {
                  case (k, v) => node.removeProperty(k)
                }
              }
            }
          }
        }
        "success"
      }
    }
  }


}
