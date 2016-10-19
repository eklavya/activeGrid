package com.activegrid.model

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 30/9/16.
  */

object Graph {
  trait Neo4jRep[T] {
    def toNeo4jGraph(entity: T): Node
    def fromNeo4jGraph(id: Long): Option[T]
  }
}

