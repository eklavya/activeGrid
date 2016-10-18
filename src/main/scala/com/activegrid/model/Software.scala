package com.activegrid.model

import com.activegrid.utils.ActiveGridUtils
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

    override def toNeo4jGraph(software: Software): Option[Node] = {
      logger.debug(s"In toGraph for Software: ${software}")
      val map = Map("version" -> software.version.getOrElse(None),
        "name" -> software.name,
        "provider" -> software.provider,
        "downloadURL" -> software.downloadURL.getOrElse(None),
        "port" -> software.port,
        "processNames" -> software.processNames.toArray,
        "discoverApplications" -> software.discoverApplications)

      val softwareNode = GraphDBExecutor.saveEntity[Software](label, map)
      softwareNode
    }

    override def fromNeo4jGraph(nodeId: Long): Software = {
      Software.fromNeo4jGraph(nodeId)
    }

  }

  def fromNeo4jGraph(nodeId: Long): Software = {
    val node = GraphDBExecutor.findNodeById(nodeId)
    val map = GraphDBExecutor.getProperties(node, "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
    val software = Software(Some(nodeId),
      ActiveGridUtils.getValueFromMapAs[String](map, "version"),
      map.get("name").get.asInstanceOf[String],
      map.get("provider").get.asInstanceOf[String],
      ActiveGridUtils.getValueFromMapAs[String](map, "downloadURL"),
      map.get("port").get.asInstanceOf[String],
      map.get("processNames").get.asInstanceOf[Array[String]].toList,
      map.get("discoverApplications").get.asInstanceOf[Boolean])
    software

  }

}
