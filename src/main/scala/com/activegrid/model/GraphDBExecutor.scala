package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.{Direction, DynamicRelationshipType, Node}
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

/**
  * Created by shareefn on 23/9/16.
  */
object GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  val VID: String = "id"

  def neo4jStoreDir = "./graphdb/activegrid/test"

  def createGraphNodeWithPrimitives[T <: BaseEntity ](label: String, map: Map[String, Any]): Option[Node] =

    withTx { neo =>

      val node = createNode(label)(neo)

      map.foreach { case (k, v) => node.setProperty(k, v) }

      logger.debug(s" new node ${node.getLabels}, id ${node.getId}")

      node.setProperty(VID, node.getId)

      Some(node)

    }


  def setGraphProperties(node: Node, paramName: String, paramValue: Any) =

    withTx { neo =>

      node.setProperty(paramName, paramValue)

    }

  def getGraphProperties(nodeId: Long, listOfKeys: List[String]): Map[String, Any] =

    withTx { neo =>

      val node = getNodeById(nodeId)(neo)

      listOfKeys.map(key => (key, node.getProperty(key).asInstanceOf[Any])).toMap[String, Any]

    }

  def setGraphRelationship(fromNode: Option[Node], toNode: Option[Node], relation: String) =

    withTx { neo =>

      val relType = DynamicRelationshipType.withName(relation)

      fromNode.get --> relType --> toNode.get

      /*start --> relType --> end <
       start.getSingleRelationship(relType, Direction.OUTGOING)*/
    }

  def getChildNodeId(parentNode: Long, relation: String): Long = {

    withTx { neo =>

      val node = getNodeById(parentNode)(neo)

      node.getSingleRelationship(relation, Direction.OUTGOING).getEndNode.getId

    }

  }

  def getChildNodeIds(parentNode: Long, relation: String): List[Long] = {

    withTx { neo =>

      val node = getNodeById(parentNode)(neo)

      node.getRelationships(relation, Direction.OUTGOING).map(rel => rel.getEndNode.getId).toList

    }

  }

  def deleteEntity[T <: BaseEntity : Manifest](imageId: Long): Unit = {

    withTx { neo =>

      val node = getNodeById(imageId)(neo)

      node.delete()

    }

  }

  def getNodesByLabel(label: String): List[Node] = withTx { neo =>

    getAllNodesWithLabel(label)(neo).toList

  }

  def getNodeByProperty(label: String, propertyName: String, propertyVal: Any): Option[Node] = withTx { neo =>

    val nodes = findNodesByLabelAndProperty(label, propertyName, propertyVal)(neo)
    //Make sure null is returned in case there is no node
    Some(nodes.iterator.next())

  }

}
