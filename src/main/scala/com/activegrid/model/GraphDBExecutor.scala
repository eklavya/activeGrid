package com.activegrid.model

import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}


/**
  * Created by sampathr on 22/9/16.
  */
class GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {

  def neo4jStoreDir = "./graphdb/activegrid"

  def persistEntity[T <: BaseEntity: Manifest](entity: T, label: String): Option[T] = {

    withTx { neo =>

      //val node = createNode(entity)(neo)

      val node = createNode(entity, label)(neo)
    }

    return Some(entity)
  }

  def getEntities[T:Manifest](label: String): Option[List[T]] = {

    withTx {  neo =>

      val nodes = getAllNodesWithLabel(label)(neo)

      Some(nodes.map(_.toCC[T].get).toList)

    }
  }



  def deleteEntity[T<:BaseEntity: Manifest](id: Long): Unit = {

    withTx{ neo =>

      val node = getNodeById(id)(neo)

      node.delete()

    }
  }

}
