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
      println(s" new node ${node.getLabels}, id ${node.getId}")
      println(s" imageId ${node.getProperty("imageId")}")
    }

    return Some(entity)
  }

  def getEntities[T:Manifest]: Option[List[T]] = {

    withTx {  neo =>

      val nodes = getAllNodes(neo)

      Some(nodes.map(_.toCC[T].get).toList)

    }
  }



  def deleteEntity[T<:BaseEntity: Manifest](label: String, paramName: String, paramValue:Any): Unit = {

    withTx{ neo =>

    val nodes = findNodesByLabelAndProperty(label,paramName,paramValue)(neo)
      nodes.foreach(println)
      nodes.map(_.delete())

    }
  }




}
