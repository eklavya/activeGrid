package com.activegrid.Neo4j

import com.activegrid.entities.BaseEntity

/**
  * Created by sivag on 5/10/16.
  */
trait Neo4JRepo[T <: BaseEntity] {

  def toGraph(entity: T): Unit
  def fromGraph(nodeId: Long) : T

}
