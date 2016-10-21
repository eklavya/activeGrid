package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by sivag on 5/10/16.
  */
trait Neo4JRepo[T <: BaseEntity] {

  def toGraph(entity: T): Option[Node]

  def fromGraph(nodeId: Long): T

}
