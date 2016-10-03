package com.activegrid.model

import org.neo4j.graphdb.Node

/**
  * Created by sampathr on 30/9/16.
  */
trait Neo4jRep[T] {

  //need to write implicit class for list maps and all collection things
  //
  def toGraph = ???

  def fromGraph(node: Node) : T = ???



}
/*
case class Clz1(name: String) extends Neo4jRep[Clz1] {

  override def toGraph = ???
}



case class CLz2(name: String, clz1: Clz1, list: List[Clz1]) extends Neo4jRep[CLz2] {
  override def toGraph = ???
}



class List[T <: Neo4jRep[T]] extends Neo4jRep[List[T]] {
  override def toGraph = ???
}
*/




