package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by babjik on 4/10/16.
  */
trait Neo4jRep[T] {
  def toGraph(entity: T): Option[Node]
  def fromGraph(nodeId: Long): T
}
trait Neo4jRep2[T] {

  /* Returning persisted node as return type to specify the user id for future reference */
  def toGraph(entity: T): Option[Node]

  /* Returning Option of Type to handle invalid nodeId */
  def fromGraph(nodeId: Long): Option[T]
}
