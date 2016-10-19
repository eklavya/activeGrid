package com.activegrid.neo4j

import com.activegrid.entities.AppSettings
import com.activegrid.utils.Utils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.{Node, Relationship}
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

  override def  toGraph(entity: AppSettings): Option[Node] = {
    try {
      withTx {
        neo => {
          val generalSettings = createNode(lables("GS").toString)(neo)
          val authSettings = createNode(lables("AUS").toString)(neo)
          util.setNodeProperties(generalSettings, entity.settings)
          util.setNodeProperties(authSettings, entity.authSettings)
          val appSettings = createNode(lables("AS").toString)(neo)
          appSettings --> lables("HGS").toString --> generalSettings
          appSettings --> lables("HAS").toString --> authSettings
          Some(appSettings)
        }
      }
    }
    catch {
      case ex:Exception => logger.error(ex.getMessage)
    }
    None
  }

  override def fromGraph(nodeId: Long): AppSettings = {
    val genaralSettings = scala.collection.mutable.HashMap.empty[String, String]
    val authSettings = scala.collection.mutable.HashMap.empty[String, String]
    try {
      withTx {
        neo => {
          withTx { neo =>
            val settingNodes = getAllNodesWithLabel(lables("AS").toString)(neo)
            for (n <- settingNodes) {
              logger.info(n.getAllProperties.toString)
              val iterator = n.getRelationships().iterator()
              while (iterator.hasNext) {
                val relation: Relationship = iterator.next
                logger.info("Processing " + relation.getType.name())
                if (relation.getType.name.equalsIgnoreCase(lables("HAS").toString)) {
                  logger.info("Processing " + relation.getType.name())
                  relation.getEndNode.getAllProperties.asScala.foreach{
                    case(k,v) => genaralSettings += (k -> v.toString)
                  }
                }
                if (relation.getType.name.equalsIgnoreCase(lables("HGS").toString)) {
                  logger.info("Processing " + relation.getType.name())
                  relation.getEndNode.getAllProperties.asScala.foreach{
                    case(k,v) => authSettings += (k -> v.toString)
                  }
                }
              }
            }

          }
        }
      }
    }
    catch {
      case ex:Exception => logger.error(ex.getMessage)
    }
    AppSettings(genaralSettings.toMap, authSettings.toMap)
  }

  def updateSettings(settingsMap: Map[String, String], relationName: String): String = {
    val msg = new StringBuilder("success")
    try {
      withTx {
        neo => {
          var genaralSettings = Map.empty[String, String];
          withTx { neo =>
            // "AS" is key to "Application Settings" defined in the MAP  "labels"
            // Fetching  nodes with  "Application Settings" Label
            val settingNodes = getAllNodesWithLabel(lables("AS").toString)(neo)
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
        }
      }
    } catch {
      case iae: IllegalArgumentException => logger.error("Null value passed to property")
        msg.append("failed")
      case ex: Exception => logger.error(ex.getMessage)
        msg.append("failed")

    }
    if (msg.toString().contains("failed")) {
      "FAILED"
    }
    else {
      "SUCCESS"
  }

  }

  def deleteSetting(settingsMap: Map[String, String], relationName: String): String = {
    val msg = new StringBuilder;
    try {
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
                    case (k, v) => if(node.removeProperty(k) == null) throw new IllegalArgumentException("Trying to delete null ")
                  }
                }
              }
            }
          }
        }
      }
    }
    catch {
      case iae: IllegalArgumentException => logger.error(iae.getMessage)
        msg.append("failed")
      case ex: Exception => logger.error(ex.getMessage)
        msg.append("failed")
    }
    if (msg.toString().contains("failed"))
      "FAILED"
    else
      "SUCCESS"
  }
}
