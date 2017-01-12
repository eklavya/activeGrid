package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.{ActiveGridUtils => AGU}
import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb._
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

// scalastyle:ignore underscore.import

/**
  * Created by babjik on 23/9/16.
  */

object Neo4jRepository extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def neo4jStoreDir: String = AGU.DBPATH

  def hasLabel(node: Node, label: String): Boolean = {
    withTx {
      neo =>
        node.hasLabel(label)
    }
  }

  def getProperty[T: Manifest](node: Node, name: String): Option[T] = {
    withTx {
      neo =>
        if (node.hasProperty(name)) Some(node.getProperty(name).asInstanceOf[T]) else None
    }
  }

  def getSingleNodeByLabelAndProperty(label: String, propertyKey: String, propertyValue: Any): Option[Node] = withTx { implicit neo =>
    logger.debug(s"finding $label's with property $propertyKey and value $propertyValue")
    val nodesIterator = findNodesByLabelAndProperty(label, propertyKey, propertyValue)
    nodesIterator.headOption
  }
  def getNodesByLabelAndProperty(label: String, propertyKey: String, propertyValue: Any): List[Node] = withTx { implicit neo =>
    logger.debug(s"finding $label's with property $propertyKey and value $propertyValue")
    findNodesByLabelAndProperty(label, propertyKey, propertyValue).toList

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
        val node = getNodeById(nodeId)
        logger.info(s"deleting relationships for Node : $nodeId")
        deleteRelationships(node, isStart = true)
        node
      case None =>
        logger.info(s"creating node with label $label")
        createNode(label)
    }
  }

  def getNodesByLabel(label: String): List[Node] = withTx { neo =>
    val list = getAllNodesWithLabel(label)(neo).toList
    logger.debug(s"Size of nodes with label : $label : ${list.size}")
    list
  }

  def getProperties(node: Node, keys: String*): Map[String, Any] = withTx { neo =>
    keys.foldLeft(Map[String, Any]())((accum, i) => if (node.hasProperty(i)) accum + ((i, node.getProperty(i))) else accum)
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

  def getRelationships(node: Node, direction: Direction): List[Relationship] = withTx { implicit neo =>
    logger.debug(s"fetching realtions of $node in the direction $direction")
    node.getRelationships(direction).toList
  }

  def deleteRelation(instanceId: String, parentEntity: BaseEntity, relationName: String): ExecutionStatus = withTx {
    implicit neo =>
      parentEntity.id match {
        case Some(id) => val parent = getNodeById(id)
          //Fetching relations that maps instance to site
          val relationList = parent.getRelationships(Direction.OUTGOING).toList.filter(relation => relation.getType.name.equals(relationName))

          //Deleting insatnce node and relation.
          relationList.foreach {
            relation => val instanceNode = relation.getEndNode
              if (instanceNode.getId == instanceId) {
                instanceNode.delete()
                relation.delete()
              }
          }
          ExecutionStatus(true, s"Instace ${instanceId} from ${parentEntity.id} removed successfully")
        // If parent id invalid
        case _ => ExecutionStatus(false, s"Parent node ${parentEntity.id} not available")
      }
  }

  def findNodeByLabelAndId(label: String, id: Long): Option[Node] = withTx { implicit neo =>
    val mayBeNode = findNodeById(id)

    mayBeNode match {
      case Some(node) => if (node.hasLabel(label)) mayBeNode else None
      case None => None
    }
  }
  def updateNodeByLabelAndId[T<:BaseEntity](label: String, id: Long,props:Map[String,Any]): Unit =
    withTx
    {
      implicit neo => Neo4jRepository.findNodeByLabelAndId(label,id).foreach {
      node =>
        for ((prop,value) <- props) { node.setProperty(prop,value) }
    }
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

  def setGraphRelationship(fromNode: Node, toNode: Node, relation: String): Unit = withTx { neo =>
    val relType = DynamicRelationshipType.withName(relation)
    logger.debug(s"setting relationship : $relation")
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
      list
    }
    catch {
      case ex: Exception =>
        logger.debug(s"does not have node with NodeId $parentNodeId")
        List.empty[Long]
    }
  }

  def getNodeByProperty(label: String, propertyName: String, propertyVal: Any): Option[Node] = withTx { neo =>
    val nodes = findNodesByLabelAndProperty(label, propertyName, propertyVal)(neo)
    nodes.headOption
  }

  def deleteRelationship(parentNodeId: Long, childNodeId: Long, relationshipName: String): Boolean = {
    withTx { neo =>
      val mayBeParentNode = findNodeById(parentNodeId)
      mayBeParentNode match {
        case Some(parentNode) =>
          if (parentNode.hasRelationship(relationshipName)) {
            val relationships = parentNode.getRelationships(relationshipName, Direction.OUTGOING)
            val relationshipToDelete = relationships.find(relationship => relationship.getEndNode.getId == childNodeId)
            relationshipToDelete match {
              case Some(relationship) =>
                relationship.delete()
                true
              case None => false
            }
          } else {
            false
          }
        case None => false
      }
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

  def deleteRelationships(node: Node, isStart: Boolean = false): Unit = {
    val outGoingRelations = getRelationships(node, Direction.OUTGOING)
    if (outGoingRelations.nonEmpty) {
      outGoingRelations.foreach {
        relation =>
          deleteRelationships(relation.getEndNode)
      }
    }
    val incomingRelations = getRelationships(node, Direction.INCOMING)
    incomingRelations.foreach(relation => relation.delete())
    if (!isStart) {
      node.delete()
    }
  }
}
