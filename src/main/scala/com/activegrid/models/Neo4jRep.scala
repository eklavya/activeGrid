package com.activegrid.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 30/09/16.
  */
trait Neo4jRep[T <: BaseEntity]{
  def toGraph() : Node = ???
  def fromGraph():T = ???
}
