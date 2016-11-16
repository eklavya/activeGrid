package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 28/10/16.
  */
case class ApplicationTier(override val id: Option[Long],
                           name: String,
                           description: String,
                           instances: List[Instance],
                           apmServer: APMServerDetails) extends BaseEntity

object ApplicationTier {

  implicit class ApplicationTierImpl(applicationTier: ApplicationTier) extends Neo4jRep[ApplicationTier] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "ApplicationTier"

    override def toNeo4jGraph(applicationTier: ApplicationTier): Node = {
      logger.debug(s"In toGraph for Software: $applicationTier")
      val map = Map("name" -> applicationTier.name,
        "description" -> applicationTier.description,
        "instances" -> applicationTier.instances.toArray,
        "apmServer" -> applicationTier.apmServer)

      val applicationTierNode = Neo4jRepository.saveEntity[ApplicationTier](label, applicationTier.id,map)
      applicationTierNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[ApplicationTier] = {
      ApplicationTier.fromNeo4jGraph(nodeId)
    }

  }

  def fromNeo4jGraph(nodeId: Long): Option[ApplicationTier] = {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    try {
      val node = Neo4jRepository.findNodeById(nodeId)
      val map = Neo4jRepository.getProperties(node.get, "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
      val applicationTier = ApplicationTier(Some(nodeId),
        map("name").asInstanceOf[String],
        map("description").asInstanceOf[String],
        map("instances").asInstanceOf[List[Instance]].toList,
        map("apmServer").asInstanceOf[APMServerDetails])
      Some(applicationTier)
    } catch {
      case ex: Exception =>
        logger.warn(ex.getMessage, ex)
        None

    }
  }
}
