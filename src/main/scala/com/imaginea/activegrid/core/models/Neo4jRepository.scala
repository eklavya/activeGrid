package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb._
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.util.Try

/**
  * Created by babjik on 23/9/16.
  */
object Neo4jRepository extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def neo4jStoreDir = "./graphdb/activegriddb"

  def getSingleNodeByLabelAndProperty(label: String, propertyKey: String, propertyValue: Any): Option[Node] = withTx { implicit neo =>
    logger.debug(s"finding $label's with property $propertyKey and value $propertyValue")
    val nodesIterator = findNodesByLabelAndProperty(label, propertyKey, propertyValue)
    nodesIterator.headOption
  }

  def saveEntity[T <: BaseEntity](label: String, id: Option[Long], map: Map[String, Any]): Node = withTx { implicit neo =>
    val node = getOrSaveEntity(label, id)
    map.foreach { case (key, value) =>
      logger.debug(s"Setting property to $label[${node.getId}]  $key -> $value")
      value match {
        case None => if (node.hasProperty(key)) node.removeProperty(key)
        case Some(x) => node.setProperty(key, x)
        case _ => node.setProperty(key, value)
      }
    }
    node
  }

  def getOrSaveEntity(label: String, id: Option[Long]): Node = withTx { implicit neo =>
    id match {
      case Some(nodeId) =>
        logger.info(s"fetching node with Id $nodeId")
        getNodeById(nodeId)
      case None =>
        logger.info(s"creating node with label $label")
        createNode(label)
    }
  }

  def getNodesByLabel(label: String): List[Node] = withTx { neo =>
    getAllNodesWithLabel(label)(neo).toList
  }

  def getProperties(node: Node, keys: String*): Map[String, Any] = withTx { neo =>
    keys.foldLeft(Map[String, Any]())((accum, i) => if(node.hasProperty(i))  accum + ((i, node.getProperty(i))) else accum)
  }

  def deleteChildNode(nodeId: Long): Option[Boolean] = withTx { implicit neo =>

    // get realtion with parent  (incoming relations)
    // delete incoming
    // delete node
    logger.debug(s"trying to delete node with id $nodeId")
    val node = getNodeById(nodeId)
    val incomingRelations = getRelationships(node, Direction.INCOMING)
    incomingRelations.foreach(incomingRelation => {
      logger.debug(s"Deleting incoming relation $incomingRelation  -- ${incomingRelation.getType}")
      incomingRelation.delete()
    })

    deleteEntity(nodeId)
    Some(true)
  }


  def deleteEntity(nodeId: Long): Unit = withTx { implicit neo =>
    val mayBeNode = findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val relations = getRelationships(node, Direction.OUTGOING)
        logger.debug(s"found relations for node $nodeId - relations $relations")

        relations.foreach(outRelation => {
          logger.debug(s"deleting out relation $outRelation  -- ${outRelation.getType}")
          outRelation.delete()
        })

        logger.debug(s"finally deleting node $node")
        node.delete()
      case None =>
        logger.warn(s"Node with id $nodeId doesnot exists")
    }

  }

  def findNodeById(id: Long): Option[Node] = withTx { implicit neo =>
    try {
      Some(getNodeById(id))
    } catch {
      case e: NotFoundException =>
        logger.warn(s"node with Id $id is not found", e)
        None
    }
  }

  def findNodeByLabelAndId(label: String, id: Long): Option[Node] = withTx { implicit neo =>
    val mayBeNode = findNodeById(id)

    mayBeNode match {
      case Some(node) => if (node.hasLabel(label)) mayBeNode else None
      case None => None
    }
  }

  def getRelationships(node: Node, direction: Direction): List[Relationship] = withTx { implicit neo =>
    logger.debug(s"fetching realtions of $node in the direction $direction")
    node.getRelationships(direction).toList
  }

  def createRelation(label: String, fromNode: Node, toNode: Node): Relationship = withTx { neo =>
    logger.info(s"Relation:  ($fromNode) --> $label --> ($toNode) <")
    val relType = DynamicRelationshipType.withName(label)
    val relation: Relationship = fromNode.createRelationshipTo(toNode, relType)
    logger.debug(s"New Relation is ${relation.getType} [${relation.getId}]")
    relation
  }

  def getNodesWithRelation(fromNode: Node, relationLabel: String): List[Node] = withTx { neo =>
    logger.debug(s"Checking for the child's of $fromNode  with relation $relationLabel")
    val relType = DynamicRelationshipType.withName(relationLabel)
    fromNode.getRelationships(relType, Direction.OUTGOING).map(rel => rel.getEndNode).toList
  }

}
