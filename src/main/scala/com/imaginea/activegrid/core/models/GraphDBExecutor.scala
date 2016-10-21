package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.{Node, NotFoundException}
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 22/9/16.
  */
object GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val V_ID = "V_ID"

  def neo4jStoreDir = "./graphdb/activegrid"

  def findNodeById(id: Long): Option[Node] = withTx { neo =>
    try {
      Some(getNodeById(id)(neo))
    } catch {
      case e: NotFoundException =>
        logger.warn(s"node with Id $id is not found", e)
        None
    }
  }

  def getProperties(node: Node, keys: String*): Map[String, Any] = withTx { neo =>
    keys.foldLeft(Map[String, Any]())((accum, i) => if (node.hasProperty(i)) accum + ((i, node.getProperty(i))) else accum)
  }

  def saveEntity[T <: BaseEntity](label: String, map: Map[String, Any]): Node = withTx { implicit neo =>
    val node = createNode(label)
    map.foreach { case (k, v) =>
      logger.debug(s"Setting property to $label[${node.getId}]  $k -> $v")

      v match {
        case None =>
          if (node.hasProperty(k)) node.removeProperty(k)
        case Some(x) => node.setProperty(k, x)
        case _ => node.setProperty(k, v)
      }
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
