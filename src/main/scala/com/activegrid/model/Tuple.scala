package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */

case class Tuple(override val id: Option[Long],key: String, value: String)  extends BaseEntity

object  Tuple{

  implicit class TupleImpl(tuple: Tuple) extends Neo4jRep[Tuple]{

    override def toNeo4jGraph(entity: Tuple): Option[Node] = {

      val label = "Tuple"

      val mapPrimitives = Map("key" -> entity.key, "value" -> entity.value)

      val node = GraphDBExecutor.createGraphNodeWithPrimitives[Tuple](label, mapPrimitives)

      node

    }

    override def fromNeo4jGraph(nodeId: Long): Tuple = {

      val listOfKeys = List("key","value")

      val propertyValues  = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val key = propertyValues.get("key").get.toString
      val value =  propertyValues.get("value").get.toString

      Tuple(Some(nodeId),key,value)

    }

  }

}