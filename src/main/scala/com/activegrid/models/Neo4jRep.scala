package com.activegrid.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 30/09/16.
  */
trait Neo4jRep[T <: BaseEntity] {
  def toNeo4jGraph(): Option[Node]

  def fromNeo4jGraph(nodeId: Option[Long]): Option[T]
}
