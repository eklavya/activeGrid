package com.activegrid.models

import org.neo4j.graphdb.{Direction, Node, Relationship, RelationshipType}

import scala.collection.mutable


/**
  * Created by nagulmeeras on 27/09/16.
  */

case class AppSettings(id: Int,
                       createdAt: String,
                       createdBy: String,
                       lastUpdatedAt: String,
                       lastUpdatedBy: String,
                       list: List[Setting]) extends BaseEntity

object Implicits {
  val repo = Neo4JRepo
  implicit class AppSettingsImpl(entity: AppSettings) extends Neo4jRep[AppSettings] {
    val labelName = "AppSettings"
    override def toGraph(): Node = {

      repo.withTx { neo =>
        val node: Node = repo.createNode(labelName)(neo)
        node.setProperty("id", entity.id)
        node.setProperty("createdAt", entity.createdAt)
        node.setProperty("createdBy", entity.createdBy)
        node.setProperty("lastUpdatedAt", entity.lastUpdatedAt)
        node.setProperty("lastUpdatedBy", entity.lastUpdatedBy)
        entity.list.foreach {
          setting =>
              val childNode = setting.toGraph()
              createRelationShip(node, childNode, "Has")
        }
        node
      }
    }

    override def fromGraph(): AppSettings = {
      var appSettings:AppSettings = null
      repo.withTx {
        neo =>
          println("Coming executing ")
          val nodes = repo.getAllNodesWithLabel("AppSettings")(neo)

          nodes.foreach {
            node =>
              val relationships = node.getRelationships
              val relationIterator = relationships.iterator()
              val settingsList: scala.collection.mutable.MutableList[Setting] = mutable.MutableList()
              while (relationIterator.hasNext){
                val relationship = relationIterator.next()
                val endNode = relationship.getEndNode;
                val setting : Setting = new Setting(endNode.getProperty("key").toString, endNode.getProperty("value").toString)
                settingsList += (setting)
              }

              appSettings = new AppSettings(Int.unbox(node.getProperty("id")),node.getProperty("createdAt").toString,node.getProperty("createdBy").toString
                ,node.getProperty("lastUpdatedAt").toString,node.getProperty("lastUpdatedBy").toString,settingsList.toList)

          }
      }

      appSettings
    }
    def getAppSettingNode():Node = {
      repo.withTx{
        neo => val nodes = repo.getAllNodesWithLabel(labelName)(neo)
          nodes.head
      }
    }
  }

  implicit class SettingsImpl(setting : Setting) extends Neo4jRep[Setting]{
    val labelName = "Setting"
    override def toGraph(): Node = {
      repo.withTx{
        neo =>
          val node = repo.createNode(labelName)(neo)
          node.setProperty("key", setting.key)
          node.setProperty("value", setting.value)
          node
      }
    }

  }

  implicit def createRelationShip(parentNode : Node, childNode : Node , relationship : String): Unit ={
    repo.withTx{
      neo =>
        parentNode.createRelationshipTo(childNode , new RelationshipType {
          override def name(): String = relationship
        })
    }
  }

}


