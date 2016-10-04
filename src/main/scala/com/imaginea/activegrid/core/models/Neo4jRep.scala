package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by babjik on 4/10/16.
  */
trait Neo4jRep[T] {
  def toGraph(entity: T): Option[Node] = ???
  def fromGraph(nodeId: Long): T = ???
}
