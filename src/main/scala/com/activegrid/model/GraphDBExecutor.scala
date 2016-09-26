package com.activegrid.model

import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}


/**
  * Created by shareefn on 23/9/16.
  */
class GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider{

  def neo4jStoreDir = "./graphdb/activegrid"

  def persistEntity[T <: BaseEntity: Manifest](entity: T, label: String): Option[T] = {

    withTx { neo =>

      //val node = createNode(entity)(neo)

      val node = createNode(entity, label)(neo)

    }

    return Some(entity)
  }

  def getEntities[T:Manifest]: Option[List[T]] = {

    withTx {  neo =>

      val nodes = getAllNodes(neo)

      Some(nodes.map(_.toCC[T].get).toList)

    }
  }



  def deleteEntity[T<:BaseEntity: Manifest]( id:Long): Unit = {

    withTx{ neo =>


    }
  }




}
