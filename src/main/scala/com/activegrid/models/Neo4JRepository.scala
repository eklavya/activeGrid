package com.activegrid.models

import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 03/10/16.
  */
object Neo4JRepository extends Neo4jWrapper with EmbeddedGraphDatabaseServiceProvider {
  override def neo4jStoreDir = "./activeGridDB"

  def hasLabel(node: Node, label: String): Boolean = {
    node.hasLabel(label)
  }
}
