package com.activegrid.model

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 7/10/16.
  */
case class StorageInfo(override val id: Option[Long], used: Double, total: Double) extends BaseEntity

object StorageInfo {

  def fromNeo4jGraph(id: Option[Long]): Option[StorageInfo] = {
    id match {
      case Some(nodeId) =>
        val listOfKeys = List("used", "total")
        val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
        val used: Double = propertyValues("used").toString.toDouble
        val total: Double = propertyValues("total").toString.toDouble
        Some(StorageInfo(Some(nodeId), used, total))

      case None => None
    }
  }

  implicit class StorageInfoImpl(storageInfo: StorageInfo) extends Neo4jRep[StorageInfo] {

    override def toNeo4jGraph(entity: StorageInfo): Node = {
      val label = "StorageInfo"
      val mapPrimitives = Map("used" -> entity.used, "total" -> entity.total)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[StorageInfo](label, mapPrimitives)

      node
    }

    override def fromNeo4jGraph(id: Option[Long]): Option[StorageInfo] = {
      StorageInfo.fromNeo4jGraph(id)
    }

  }

}