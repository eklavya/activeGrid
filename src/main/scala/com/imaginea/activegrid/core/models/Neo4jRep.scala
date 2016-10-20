package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
 * Created by babjik on 4/10/16.
 */
trait Neo4jRep[T] {

  /* Returning persisted node as return type to specify the user id for future reference */
  def toNeo4jGraph(entity: T): Node

  /* Returning Option of Type to handle invalid nodeId */
  def fromNeo4jGraph(nodeId: Long): Option[T]
}

