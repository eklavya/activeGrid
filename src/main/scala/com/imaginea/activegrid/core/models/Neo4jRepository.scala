package com.imaginea.activegrid.core.models

import java.lang.Iterable

import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.{Direction, DynamicRelationshipType, Node, Relationship}
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 23/9/16.
  */
object Neo4jRepository extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider{
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  def neo4jStoreDir = "./graphdb/activegriddb"

  // constants
  val V_ID = "V_ID"
  val CLASS = "@Class"


  def findNodeById(id: Long): Node = withTx { neo =>
      getNodeById(id)(neo)
  }

  def saveEntity[T <: BaseEntity](label: String, map: Map[String, Any]): Option[Node] = withTx { implicit neo =>
    val node = createNode(label)
    map.foreach{ case(k, v) => {
      logger.debug(s"Setting property to $label[${node.getId}]  $k -> $v" )
      node.setProperty(k, v)
    }}

    // setting Id and class Name as attributes
    node.setProperty(V_ID, node.getId)
    node.setProperty(CLASS, label)

    Some(node)
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
    * @param label
    * @param propertyName
    * @param propertyValue
    * @tparam T
    */
  def deleteEntity[T: Manifest](label: String, propertyName: String, propertyValue: Any): Unit = withTx { neo =>
    val nodes = findNodesByLabelAndProperty(label, propertyName, propertyValue) (neo)
    nodes.foreach(node=> {
      logger.debug(s"Deleting node of type $label, id ${node.getId}")
      logger.debug(s"checking with property $propertyName and value $propertyValue")

      node.hasRelationship() match {
        case true => {
          val relations: Iterable[Relationship] = node.getRelationships()
          logger.debug(s"relations found are $relations")

        }
        case false => logger.debug(s"no relations found for this entity $label, id ${node.getId}")
      }
    })

    nodes.map(_.delete())
  }


  // with out properties
  def createRelation(label: String, fromNode: Node, toNode: Node) : Relationship = withTx {neo =>
    logger.debug(s"Relation:  (${fromNode}) --> ${label} --> (${toNode}) <")
    val relType = DynamicRelationshipType.withName(label)
    val relation: Relationship = fromNode.createRelationshipTo(toNode, relType)
    logger.debug(s"New Relation is ${relation.getType} [${relation.getId}]")
    relation
  }


  def getNodesWithRelation(fromNode: Node, relation: String): List[Node] = withTx {neo =>
    val relType = DynamicRelationshipType.withName(relation)
    val relations = fromNode.getRelationships(relType, Direction.OUTGOING).iterator()
    val childNodes: scala.collection.mutable.ListBuffer[Node] = scala.collection.mutable.ListBuffer()

    while (relations.hasNext) {
      val relation = relations.next()
      childNodes += (relation.getEndNode)
    }

    childNodes.toList
  }

}
