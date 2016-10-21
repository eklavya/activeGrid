package com.imaginea.activegrid.core.models

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

  def getProperty[T: Manifest](node: Node, name: String): Option[T] = {
    if (node.hasProperty(name)) Some(node.getProperty(name).asInstanceOf[T]) else None
  }
}
