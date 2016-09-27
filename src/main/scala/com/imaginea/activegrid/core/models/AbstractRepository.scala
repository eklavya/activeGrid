package com.imaginea.activegrid.core.models

import java.lang.Iterable

import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.Relationship
import org.slf4j.LoggerFactory

/**
  * Created by babjik on 23/9/16.
  */
trait AbstractRepository extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider{
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  def neo4jStoreDir = "./graphdb/activegriddb"


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
    logger.debug(s"picking done ${nodesList.size}")
    val entities = nodesList.map(_.toCC[T].get).toList
    logger.debug(s"conversion is done to type $entities")
    entities
  }

  /**
    * Returnts the Entity By Graph Node Id
    * @param id
    * @tparam T
    * @return
    */
  def getEntityByNodeId[T: Manifest] (id: Long) : Option[T] = withTx { neo =>
   val node =  getNodeById(id)(neo)
    Some(node.toCC[T].get)
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

}
