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

  def findNodeById(id: Long): Node = withTx { neo =>
    getNodeById(id)(neo)
  }

  def getProperties(node: Node, keys: String*): Map[String, Any] = withTx { neo =>
    val map: scala.collection.mutable.Map[String, AnyRef] = scala.collection.mutable.Map()
    keys.foreach(key => {
      map += ((key, node.getProperty(key)))
    })
    map.toMap
  }

  def saveEntity[T <: BaseEntity](label: String, map: Map[String, Any]): Node = withTx { implicit neo =>
    val node = createNode(label)
    map.foreach { case (k, v) =>
      logger.debug(s"Setting property to $label[${node.getId}]  $k -> $v")

      v match {
        case None =>
          if (node.hasProperty(k)) node.removeProperty(k)
        case _ => node.setProperty(k, v)
      }
      node.setProperty(k, v)
    }
    node.setProperty(V_ID, node.getId)
    node
  }

  def getNodesByLabel(label: String): List[Node] = withTx { neo =>
    val nodesList = getAllNodesWithLabel(label)(neo).toList
    logger.debug(s"Number of nodes Fetched with : $label : ${nodesList.size}")
    nodesList
  }

  def deleteEntity[T <: BaseEntity : Manifest](id: Long): Unit = {
    withTx { neo =>
      val node = getNodeById(id)(neo)
      node.delete()
    }
  }
}
