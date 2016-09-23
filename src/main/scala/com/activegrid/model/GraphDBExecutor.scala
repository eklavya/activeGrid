package com.activegrid.model

import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}

/**
  * Created by shareefn on 23/9/16.
  */
class GraphDBExecutor extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider{

  def neo4jStoreDir = "./graphdb/activegrid"

  def persistEntity(imageInfo: ImageInfo): Option[ImageInfo] = {

    withTx { neo =>

      val node = createNode(imageInfo)(neo)

    }

    return Some(imageInfo)
  }

  def getEntities: Option[List[ImageInfo]] = {

    withTx {  neo =>

      val nodes = getAllNodes(neo)

      Some(nodes.map(_.toCC[ImageInfo].get).toList)

    }
  }


}
