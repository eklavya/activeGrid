package com.activegrid.model

import eu.fakod.neo4jscala.{EmbeddedGraphDatabaseServiceProvider, Neo4jWrapper}
import org.neo4j.graphdb.Node
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by shareefn on 30/9/16.
  */

object Graph  {



  trait Neo4jRep[T] {
    def toGraph(entity: T): Option[Node] = ???

    def fromGraph(nodeId: Long): Option[T] = ???
  }


}

