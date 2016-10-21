package com.activegrid.neo4j

import com.activegrid.entities.AppSettings
import com.activegrid.models.ExecutionStatus
import com.activegrid.utils.Utils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.{Node, Relationship}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by sivag on 6/10/16.
  */
object AppSettingsNeo4jWrapper  extends DBWrapper {

  val lables: HashMap[String, String] = HashMap[String, String]("GS" -> "GeneralSettings", "AS" -> "AppSettings", "AUS" -> "AuthSettings", "HAS" -> "HAS_AUTH_SETTINGS", "HGS" -> "HAS_GENERAL_SETTINGS")
  val util = new Utils()
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def toNeo4jGraph(entity: AppSettings): Option[Node] = {
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

   def fromNeo4jGraph(nodeId: Long): AppSettings =  {
     withTx {
       neo => {
         withTx { neo =>
           val settingNodes = getAllNodesWithLabel(lables("AS").toString)(neo).toList
           settingNodes.map{node => AppSettings(getSettingsByRelation(node, lables("HGS").toString), getSettingsByRelation(node, lables("HAS").toString))}.head
         }
       }
     }
   }

  def updateSettings(settingsToUpdate: Map[String, String], settingsType: String): Future[ExecutionStatus] = {
    if(settingsType.equalsIgnoreCase("AUTH_SETTINGS"))
     updateOrDeleteSettings(settingsToUpdate,lables("HAS").toString,"UPDATE")
    else
      updateOrDeleteSettings(settingsToUpdate,lables("HGS").toString,"UPDATE")

  }
  def deleteSetting(settingsToDelete: Map[String, String], settingsType: String): Future[ExecutionStatus] = {
    if(settingsType.equalsIgnoreCase("AUTH_SETTINGS"))
      updateOrDeleteSettings(settingsToDelete,lables("HAS").toString,"DELETE")
    else
      updateOrDeleteSettings(settingsToDelete,lables("HGS").toString,"DELETE")
  }
  def getSettingsByRelation(rootNode:Node, relationName:String): Map[String,String] = {
    val relationNode = getRelationNodeByName(rootNode,relationName)
    relationNode match {
      case Some(node) => node.getAllProperties.mapValues(_.toString()).toMap[String,String]
      case None => Map.empty[String,String]
    }
  }

  def getRelationNodeByName(rootNode:Node, relationName:String): Option[Node] = {
    rootNode.getRelationships.filter(relation => relation.getType.name() == relationName).headOption match {
      case Some(relationship) => Some(relationship.getEndNode)
      case None => None
    }
  }
  def updateOrDeleteSettings(settings:Map[String,String], relationName:String, updateOrDelete:String): Future[ExecutionStatus] = Future{
    withTx {
      neo => {
        withTx { neo =>
          val settingNodes = getAllNodesWithLabel(lables("AS").toString)(neo).headOption
          settingNodes match {
            case Some(rootNode) =>  val relationNode  = getRelationNodeByName(rootNode,relationName)
              relationNode match {
                case Some(dbnode) =>
                  val todelete = if(updateOrDelete.equalsIgnoreCase("DELETE")) true else false
                  settings.foreach {
                  case (k, v) =>
                    if(todelete)
                       dbnode.removeProperty(k)
                    else
                      dbnode.setProperty(k,v.toString)
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
}
