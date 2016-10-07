package com.activegrid.model

import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 22/9/16.
  */
object GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val V_ID = "V_ID"

  def neo4jStoreDir = "./graphdb/activegrid"

  def persistEntity[T <: BaseEntity : Manifest](entity: T, label: String): Option[T] = {
    withTx { neo =>
      val node = createNode(entity, label)(neo)
    }
    return Some(entity)
  }

  def findNodeById(id: Long): Node = withTx { neo =>
    getNodeById(id)(neo)
  }

  def getProperties(node: Node, keys: String*): Map[String, Any] = withTx { neo =>
    val map: scala.collection.mutable.Map[String, AnyRef] = scala.collection.mutable.Map()
    keys.foreach(key => {
      println(s" (${key}) --> (${node.getProperty(key)}) ")
      map += ((key, node.getProperty(key)))
    })
    map.toMap
  }

  def createGraphNode[T <: BaseEntity : Manifest](entity: T, label: String): Option[Node] =
    withTx { neo =>
      val node = createNode(label)(neo)
      node.setProperty("", "")
      Some(node)
    }

  def saveEntity[T <: BaseEntity](label: String, map: Map[String, Any]): Option[Node] = withTx { implicit neo =>
    val node = createNode(label)

    map.foreach { case (k, v) => {
      logger.debug(s"Setting property to $label[${node.getId}]  $k -> $v")
      node.setProperty(k, v)
    }
    }
    node.setProperty(V_ID, node.getId)
    Some(node)
  }

  def getNodesByLabel(label: String): List[Node] = withTx { neo =>
    getAllNodesWithLabel(label)(neo).toList
  }

  def getEntities[T: Manifest](label: String): Option[List[T]] = {
    withTx { neo =>
      val nodes = getAllNodesWithLabel(label)(neo)
      Some(nodes.map(_.toCC[T].get).toList)
    }
  }

  def deleteEntity[T <: BaseEntity : Manifest](id: Long): Unit = {
    withTx { neo =>
      val node = getNodeById(id)(neo)
      node.delete()
    }
  }

  def getEntity[T <: BaseEntity : Manifest](id: Long): Option[T] = {
    withTx { neo =>
      val node = getNodeById(id)(neo)
      node.toCC[T]
    }
  }

  def createEmptyGraphNode[T <: BaseEntity : Manifest](label: String, map: Map[String, Any]): Option[Node] =
    withTx { neo =>
      val node = createNode(label)(neo)
      map.foreach { case (k, v) => node.setProperty(k, v) }
      println(s" new node ${node.getLabels}, id ${node.getId}")
      println(s" Id ${node.getProperty("id")}")
      println(s" Name ${node.getProperty("name")}")
      Some(node)
    }
}
