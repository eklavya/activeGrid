package com.activegrid.model

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonInclude}
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 22/9/16.
  */
@JsonIgnoreProperties(ignoreUnknown = true)
case class Software(override val id: Option[Long],
                     @JsonInclude(JsonInclude.Include.NON_NULL) version: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) name: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) provider: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) downloadURL: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) port: String,
                     @JsonInclude(JsonInclude.Include.NON_NULL) processNames: List[String],
                     @JsonInclude(JsonInclude.Include.NON_NULL) discoverApplications: Boolean) extends BaseEntity


object Software{

  implicit class SoftwareImpl(software: Software) extends Neo4jRep[Software] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "SoftwaresTest1"

    override def toNeo4jGraph(software: Software): Option[Node] = {
      logger.debug(s"In toGraph for Software: ${software}")
      val map: Map[String, Any] = Map("version" -> software.version,
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
      val node = GraphDBExecutor.findNodeById(nodeId)
      val map = GraphDBExecutor.getProperties(node, "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
      val software = Software(Some(nodeId),
        map.get("version").get.asInstanceOf[String],
        map.get("name").get.asInstanceOf[String],
        map.get("provider").get.asInstanceOf[String],
        map.get("downloadURL").get.asInstanceOf[String],
        map.get("port").get.asInstanceOf[String],
        map.get("processNames").get.asInstanceOf[Array[String]].toList,
        map.get("discoverApplications").get.asInstanceOf[Boolean])
      Some(software)
    }
  }

}
