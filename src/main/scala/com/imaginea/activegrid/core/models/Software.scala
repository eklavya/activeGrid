package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 22/9/16.
  */
case class Software(override val id: Option[Long],
                    version: Option[String],
                    name: String,
                    provider: String,
                    downloadURL: Option[String],
                    port: String,
                    processNames: List[String],
                    discoverApplications: Boolean) extends BaseEntity

object Software {

  implicit class SoftwareImpl(software: Software) extends Neo4jRep[Software] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "SoftwaresTest2"

    override def toNeo4jGraph(software: Software): Node = {
      logger.debug(s"In toGraph for Software: $software")
      val map = Map("version" -> software.version,
        "name" -> software.name,
        "provider" -> software.provider,
        "downloadURL" -> software.downloadURL,
        "port" -> software.port,
        "processNames" -> software.processNames.toArray,
        "discoverApplications" -> software.discoverApplications)

      val softwareNode = GraphDBExecutor.saveEntity[Software](label, map)
      softwareNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[Software] = {
      Software.fromNeo4jGraph(nodeId)
    }

  }

  def fromNeo4jGraph(nodeId: Long): Option[Software] = {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    try {
      val node = GraphDBExecutor.findNodeById(nodeId)
      val map = GraphDBExecutor.getProperties(node.get, "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
      val software = Software(Some(nodeId),
        ActiveGridUtils.getValueFromMapAs[String](map, "version"),
        map("name").asInstanceOf[String],
        map("provider").asInstanceOf[String],
        ActiveGridUtils.getValueFromMapAs[String](map, "downloadURL"),
        map("port").asInstanceOf[String],
        map("processNames").asInstanceOf[Array[String]].toList,
        map("discoverApplications").asInstanceOf[Boolean])
      Some(software)
    } catch {
      case ex: Exception =>
        logger.warn(ex.getMessage, ex)
        None
    }
  }

}
