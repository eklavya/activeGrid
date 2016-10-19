package com.activegrid.model

import org.neo4j.graphdb.Node

/**
  * Created by sampathr on 30/9/16.
  */
trait Neo4jRep[T] {
  
  def toNeo4jGraph(entity: T): Option[Node]

  def fromNeo4jGraph(nodeId: Long): Option[T]
}