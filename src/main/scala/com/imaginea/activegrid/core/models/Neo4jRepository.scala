package com.imaginea.activegrid.core.models

import java.lang.Iterable

import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * Created by babjik on 23/9/16.
  */
object Neo4jRepository extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider{
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  def neo4jStoreDir = "./graphdb/activegriddb"

  // constants
  val V_ID = "id"
  val CLASS = "@Class"


  def findNodeById(id: Long): Option[Node] = withTx { implicit neo =>
    try {
      Some(getNodeById(id))
    } catch {
      case e: NotFoundException => {
        logger.warn(s"node with Id ${id} is not found", e)
        throw e
      }
    }
  }

  /**
    * Finding the first Node with given label and property details
    * @param label
    * @param propertyKey
    * @param propertyValue
    * @return
    */
  def getSingleNodeByLabelAndProperty(label: String, propertyKey: String, propertyValue: Any) : Option[Node] = withTx { implicit  neo =>
    logger.debug(s"finding ${label}'s with property ${propertyKey} and value ${propertyValue}")
    val nodesIterator = findNodesByLabelAndProperty(label, propertyKey, propertyValue)
    logger.debug(s"result size ${nodesIterator}  - ${nodesIterator.size}")
    nodesIterator.size match {
      case x if x > 0 => Some(nodesIterator.toList.head)
      case _ => None
    }

  }

  def saveEntity[T <: BaseEntity](label: String, id: Option[Long], map: Map[String, Any]): Option[Node] = withTx { implicit neo =>
    val node = getOrSaveEntity(label, id)
    map.foreach{ case(k, v) => {
      logger.debug(s"Setting property to $label[${node.getId}]  $k -> $v" )
      node.setProperty(k, v)
    }}

    // setting Id and class Name as attributes
    node.setProperty(V_ID, node.getId)
    node.setProperty(CLASS, label)

    Some(node)
  }

  def getOrSaveEntity(label: String, id: Option[Long]) : Node = withTx { implicit neo =>
    id match {
      case Some(nodeId) => {
        logger.info(s"fetching node with Id ${nodeId}")
        getNodeById(nodeId)
      }
      case None => {
        logger.info(s"creating node with label ${label}")
        createNode(label)
      }
    }
  }

  /**
    * Saves entity of type T and returns the same entity
    * @param entity
    * @param label
    * @tparam T
    * @return
    */
  def saveEntity[T <: BaseEntity: Manifest] (entity: T, label: String): T = withTx { neo =>
    val node = createNode(entity, label) (neo)
    logger.debug(s"created entity of label ${node.getLabels}, new node Id is ${node.getId} ")
    entity
  }

  /**
    *
    * @param label
    * @tparam T
    * @return
    */
  def getEntityList[T: Manifest](label: String): List[T]= withTx { neo =>
    logger.debug(s"picking the list for $label")
    val nodesList = getAllNodesWithLabel(label) (neo)
    nodesList.foreach(node => logger.debug(s"${node.getId} -> ${node.getLabels} -- properties ${node.getAllProperties}"))
    nodesList.map(_.toCC[T].get).toList
  }


  def getNodesByLabel(label: String): List[Node] = withTx {neo =>
    getAllNodesWithLabel(label)(neo).toList
  }

  def getProperties(node: Node, keys: String*): Map[String, Any] = withTx {neo =>
    val map: scala.collection.mutable.Map[String, AnyRef] = scala.collection.mutable.Map()
    keys.foreach(key => {
      logger.debug(s" (${key}) --> (${node.getProperty(key)}) ")
      map += ((key, node.getProperty(key)))
    })
    map.toMap
  }


  /**
    * Finds the node with given node and property information
    * @param label
    * @param propertyName
    * @param propertyValue
    * @tparam T
    * @return
    */
  def getEntity[T <: BaseEntity: Manifest] (label: String, propertyName: String, propertyValue: Any): T =  withTx { neo =>
    val nodes = findNodesByLabelAndProperty(label, propertyName, propertyValue) (neo)
    nodes.map(_.toCC[T].get).toList.head
  }


  /**
    * deletes the node with the given label and propery details
    * @param nodeId
    */
  def deleteEntity(nodeId: Long): Unit = withTx {implicit neo =>
    val node = findNodeById(nodeId).get
    //TODO: need to delete relations before deleting node
    val relations = node.getRelationships(Direction.OUTGOING)
    logger.debug(s"found relations for node ${nodeId} - relations ${relations}")
    val relationsIterator = relations.iterator()
    while (relationsIterator.hasNext) {
      val relation = relationsIterator.next()
      logger.debug(s"deleting relation")
      relation.delete()
    }
    logger.debug(s"finally deleting node ${node}")
    node.delete
  }


  def deleteChildNode(node: Node): Option[Boolean] = withTx { implicit neo =>

    // get realtion with parent  (incoming relations)
    // delete incoming
    // delete node
    

    Some(true)
  }

  // with out properties
  def createRelation(label: String, fromNode: Node, toNode: Node) : Relationship = withTx {neo =>
    logger.info(s"Relation:  (${fromNode}) --> ${label} --> (${toNode}) <")
    val relType = DynamicRelationshipType.withName(label)
    val relation: Relationship = fromNode.createRelationshipTo(toNode, relType)
    logger.debug(s"New Relation is ${relation.getType} [${relation.getId}]")
    relation
  }


  def getNodesWithRelation(fromNode: Node, relationLabel: String): List[Node] = withTx { neo =>
    logger.debug(s"Checking for the child's of ${fromNode}  with relation ${relationLabel}")
    val relType = DynamicRelationshipType.withName(relationLabel)
    val relations = fromNode.getRelationships(relType, Direction.OUTGOING).iterator()
    val childNodes: scala.collection.mutable.ListBuffer[Node] = scala.collection.mutable.ListBuffer()

    while (relations.hasNext) {
      val relation = relations.next()
      logger.debug(s"found relation ${relation}")
      childNodes += (relation.getEndNode)
    }

    childNodes.toList
  }

}
