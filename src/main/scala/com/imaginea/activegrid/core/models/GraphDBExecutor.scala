package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.{Direction, DynamicRelationshipType, Node, NotFoundException}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
  * Created by shareefn on 23/9/16.
  */
object GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val V_ID = "V_ID"
  val NO_VAL: String = "VALUE_DOES_NOT_EXIST"

  def neo4jStoreDir = "./graphdb/activegrid/test"

  def createGraphNodeWithPrimitives[T <: BaseEntity](label: String, map: Map[String, Any]): Node = withTx { neo =>
    val node = createNode(label)(neo)
    map.foreach { case (k, v) => node.setProperty(k, v) }
    logger.debug(s" new node of ${node.getLabels}, created with id ${node.getId}")
    node.setProperty(V_ID, node.getId)
    node
  }

  def setGraphProperties(node: Node, paramName: String, paramValue: Any) = withTx { neo =>
    node.setProperty(paramName, paramValue)
  }

  def getGraphProperties(nodeId: Long, listOfKeys: List[String]): Map[String, Any] = withTx { neo =>
    try {
      val node = getNodeById(nodeId)(neo)
      listOfKeys.
        foldLeft(Map[String, Any]())((accum, key) => if (node.getProperty(key) != NO_VAL) accum + ((key, node.getProperty(key))) else accum)
    }
    catch {
      case ex: Exception =>
        logger.debug(s"does not have node with id : $nodeId")
        Map.empty[String, Any]
    }
  }

  def setGraphRelationship(fromNode: Node, toNode: Node, relation: String) = withTx { neo =>
    val relType = DynamicRelationshipType.withName(relation)
    logger.debug(s"setting relationhip : $relation")
    fromNode --> relType --> toNode
    /*start --> relType --> end <
     start.getSingleRelationship(relType, Direction.OUTGOING)*/
  }

  def getChildNodeId(parentNode: Long, relation: String): Option[Long] = withTx { neo =>
    val node = getNodeById(parentNode)(neo)
    try {
      Some(node.getSingleRelationship(relation, Direction.OUTGOING).getEndNode.getId)
    }
    catch {
      case ex: Exception =>
        logger.debug(s"does not have relationship $relation")
        None
    }
  }

  def getChildNodeIds(parentNodeId: Long, relation: String): List[Long] = withTx { neo =>
    try {
      val node = getNodeById(parentNodeId)(neo)
      val list = node.getRelationships(relation, Direction.OUTGOING).map(rel => rel.getEndNode.getId).toList
      logger.debug(s"$list")
      list
    }
    catch {
      case ex: Exception =>
        logger.debug(s"does not have node with NodeId $parentNodeId")
        List.empty[Long]
    }
  }

  def deleteEntity[T <: BaseEntity : Manifest](imageId: Long) = withTx { neo =>
    val node = getNodeById(imageId)(neo)
    node.delete()
  }

  def getNodesByLabel(label: String): List[Node] = withTx { neo =>
    val list = getAllNodesWithLabel(label)(neo).toList
    logger.debug(s"Size of nodes with label : $label : ${list.size}")
    list
  }

  def getNodeByProperty(label: String, propertyName: String, propertyVal: Any): Option[Node] = withTx { neo =>
    val nodes = findNodesByLabelAndProperty(label, propertyName, propertyVal)(neo)
    nodes.headOption
  }

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

}
