package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb._
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
 * Created by babjik on 23/9/16.
 */
object Neo4jRepository extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  // Constants
  val V_ID = "id"
  val CLASS = "@Class"

  def neo4jStoreDir = "./graphdb/activegriddb"

  /**
   * Finding the first Node with given label and property details
   * @param label
   * @param propertyKey
   * @param propertyValue
   * @return
   */
  def getSingleNodeByLabelAndProperty(label: String, propertyKey: String, propertyValue: Any): Node = withTx { implicit neo =>
    logger.debug(s"finding ${label}'s with property ${propertyKey} and value ${propertyValue}")
    val nodesIterator = findNodesByLabelAndProperty(label, propertyKey, propertyValue)
    logger.debug(s"result size ${nodesIterator}  - ${nodesIterator.size}")
    nodesIterator.size match {
      case x if x > 0 => nodesIterator.toList.head
      case _ => throw new NotFoundException(s"Unable to Find node - ${label} with ${propertyKey} property")
    }
  }

  def saveEntity[T <: BaseEntity](label: String, id: Option[Long], map: Map[String, Any]): Node = withTx { implicit neo =>

    val node = getOrSaveEntity(label, id)
    map.foreach { case (k, v) =>
      logger.debug(s"Setting property to $label[${node.getId}]  $k -> $v")
      node.setProperty(k, v)
    }

    // setting Id and class Name as attributes
    node.setProperty(V_ID, node.getId)
    node.setProperty(CLASS, label)

    node
  }

  def getOrSaveEntity(label: String, id: Option[Long]): Node = withTx { implicit neo =>
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
   *
   * @param label
   * @tparam T
   * @return
   */
  def getEntityList[T: Manifest](label: String): List[T] = withTx { neo =>
    logger.debug(s"picking the list for $label")
    val nodesList = getAllNodesWithLabel(label)(neo)
    nodesList.foreach(node => logger.debug(s"${node.getId} -> ${node.getLabels} -- properties ${node.getAllProperties}"))
    nodesList.map(_.toCC[T].get).toList
  }

  def getNodesByLabel(label: String): List[Node] = withTx { neo =>
    getAllNodesWithLabel(label)(neo).toList
  }

  def getProperties(node: Node, keys: String*): Option[Map[String, Any]] = withTx { neo =>
    // Validating the availability of property in the node
    if(keys.forall( key => node.hasProperty(key))){
      Some(keys.map(key => {
        key -> node.getProperty(key)
      }).toMap)
    } else{
      None
    }
  }

  /**
   * Finds the node with given node and property information
   * @param label
   * @param propertyName
   * @param propertyValue
   * @tparam T
   * @return
   */
  def getEntity[T <: BaseEntity : Manifest](label: String, propertyName: String, propertyValue: Any): T = withTx { neo =>
    val nodes = findNodesByLabelAndProperty(label, propertyName, propertyValue)(neo)
    nodes.map(_.toCC[T].get).toList.head
  }

  /**
   * Finds the node with given id and result Option of Entity
   * @param id
   * @tparam T
   * @return
   */
  def getEntity[T <: BaseEntity : Manifest](id: Long): Option[T] = withTx { neo =>
    val node = getNodeById(id)(neo)
    node.toCC[T]
  }

  /**
   * deletes the node with the given label and property details
   * @param nodeId
   */

  def deleteEntity(nodeId: Long): Unit = withTx { implicit neo =>
    val node = findNodeById(nodeId)
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

  def findNodeById(id: Long): Node = withTx { implicit neo =>
    try {
      getNodeById(id)
    } catch {
      case e: NotFoundException => {
        logger.warn(s"node with Id ${id} is not found", e.getMessage)
        throw e
      }
    }
  }

  /* Validating the availability of the requested node,
   * If it fails re-throwing exception otherwise do the deleting process
   */
  def removeEntity[T <: BaseEntity : Manifest](nodeId: Long): Unit = withTx { implicit neo =>
    fetchNodeById[T](nodeId).fold(
      ex => {
        logger.debug(s"Exception while removing entity ${ex.getMessage}")
        throw new Exception(ex.getMessage)
      },
      result => {
        val node = result._1
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
    )
  }

  /**
   * fetchNode find the node by id and return Either type
   * @param id
   * @return
   */
  /* Manifest is added to support for toCC method*/

  def fetchNodeById[T <: BaseEntity : Manifest](id: Long): Either[Exception, (Node, Option[T])] = withTx { neo =>
    try {
      val node = getNodeById(id)(neo)
      Right((node, node.toCC[T]))
    } catch {
      case ex: NotFoundException => Left(ex)
      case ex: Exception => Left(ex)
    }
  }

  /**
   * Deletes the Incoming / out going relations along with the node
   *
   * @param nodeId
   * @return
   */
  def deleteChildNode(nodeId: Long): Option[Boolean] = withTx { implicit neo =>

    // get relation with parent  (incoming relations)
    // delete incoming
    // delete node
    val node = getNodeById(nodeId)
    val incomingRelations = getRelationships(node, Direction.INCOMING)
    incomingRelations.foreach(incomingRelation => {
      logger.debug(s"Deleting incoming relation ${incomingRelation}  -- ${incomingRelation.getType}")
      incomingRelation.delete()
    })

    deleteEntity(nodeId)
    Some(true)
  }

  // with out properties
  def createRelation(label: String, fromNode: Node, toNode: Node): Relationship = withTx { neo =>
    logger.info(s"Relation:  (${fromNode}) --> ${label} --> (${toNode}) <")
    val relType = DynamicRelationshipType.withName(label)
    val relation: Relationship = fromNode.createRelationshipTo(toNode, relType)
    logger.debug(s"New Relation is ${relation.getType} [${relation.getId}]")
    relation
  }


  def getNodesWithRelation(fromNode: Node, relationLabel: String): List[Node] = withTx { neo =>
    logger.debug(s"Checking for the child's of ${fromNode}  with relation ${relationLabel}")
    val relType = DynamicRelationshipType.withName(relationLabel)
    fromNode.getRelationships(relType, Direction.OUTGOING).map(rel => rel.getEndNode).toList
  }


  /**
   * Returns the List of relations from the node, in given direction like  OUTGOING, INCOMING, BOTH
   *
   * @param node
   * @param direction
   * @return
   */
  def getRelationships(node: Node, direction: Direction): List[Relationship] = withTx { implicit neo =>
    logger.debug(s"fetching realtions of ${node} in the direction ${direction}")
    node.getRelationships(direction).toList
  }

}
