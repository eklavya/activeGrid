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

      val label: String = "Tuple"

      val mapPrimitives : Map[String, Any] = Map("key" -> entity.key, "value" -> entity.value)

      val node = GraphDBExecutor.createGraphNodeWithPrimitives[PortRange](label, mapPrimitives)
      node

    }

    override def fromNeo4jGraph(nodeId: Long): Option[Tuple] = {

      val listOfKeys: List[String] = List("key","value")

      val propertyValues: Map[String,Any] = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val key: String = propertyValues.get("key").get.toString
      val value: String =  propertyValues.get("value").get.toString

      Some(Tuple(Some(nodeId),key,value))
    }

  }

}