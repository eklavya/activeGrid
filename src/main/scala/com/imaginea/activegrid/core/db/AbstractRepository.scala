package com.imaginea.activegrid.core.db

import com.imaginea.activegrid.core.models.{BaseEntity, ImageInfo}
import com.typesafe.scalalogging.Logger
import eu.fakod.neo4jscala.{DatabaseService, EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
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
  def saveEntity[T <: BaseEntity] (entity: T, label: String): T= withTx { neo =>
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
    nodesList.map(_.toCC[T].get).toList
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

}
