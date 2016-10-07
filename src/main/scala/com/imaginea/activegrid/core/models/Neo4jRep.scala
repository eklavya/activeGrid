package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by babjik on 4/10/16.
  */
trait Neo4jRep[T] {
  def toNeo4jGraph(entity: T): Option[Node] = ???
  def fromNeo4jGraph(nodeId: Long): T = ???
}
