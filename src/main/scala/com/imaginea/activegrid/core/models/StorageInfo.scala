package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 7/10/16.
  */
case class StorageInfo(override val id: Option[Long], used: Double, total: Double) extends BaseEntity

object StorageInfo {

  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(nodeId: Long): Option[StorageInfo] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "used", "total")
        val used: Double = map("used").toString.toDouble
        val total: Double = map("total").toString.toDouble

        Some(StorageInfo(Some(nodeId), used, total))
      case None =>
        logger.warn(s"could not find node for StorageInfo with nodeId $nodeId")
        None
    }
  }

  implicit class StorageInfoImpl(storageInfo: StorageInfo) extends Neo4jRep[StorageInfo] {

    override def toNeo4jGraph(entity: StorageInfo): Node = {
      val label = "StorageInfo"
      val mapPrimitives = Map("used" -> entity.used, "total" -> entity.total)
      val node = Neo4jRepository.saveEntity[StorageInfo](label, entity.id, mapPrimitives)
      node
    }

    override def fromNeo4jGraph(id: Long): Option[StorageInfo] = {
      StorageInfo.fromNeo4jGraph(id)
    }
  }

}
