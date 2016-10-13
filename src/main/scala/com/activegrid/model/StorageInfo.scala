package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */
case class StorageInfo(override val id: Option[Long],used: Double, total: Double) extends BaseEntity

object StorageInfo {

  implicit class StorageInfoImpl(storageInfo: StorageInfo) extends Neo4jRep[StorageInfo] {

    override def toNeo4jGraph(entity: StorageInfo): Option[Node] = {

      val label = "StorageInfo"

      val mapPrimitives = Map("used" -> entity.used, "total" -> entity.total)

      val node = GraphDBExecutor.createGraphNodeWithPrimitives[StorageInfo](label, mapPrimitives)

      node

    }

    override def fromNeo4jGraph(nodeId: Long): StorageInfo = {

      val listOfKeys  = List("used","total")

      val propertyValues = GraphDBExecutor.getGraphProperties(nodeId,listOfKeys)
      val used: Double = propertyValues.get("used").get.toString.toDouble
      val total : Double =  propertyValues.get("total").get.toString.toDouble

      StorageInfo(Some(nodeId),used,total)

    }

  }

}