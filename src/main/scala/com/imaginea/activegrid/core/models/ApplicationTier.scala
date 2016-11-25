package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 21/11/16.
  */
case class ApplicationTier(override val id: Option[Long],
                           name: Option[String],
                           description: Option[String],
                           instances: List[Instance],
                           apmServer: Option[APMServerDetails]) extends BaseEntity

object ApplicationTier {
  val labelName = "ApplicationTier"
  val appTierAndInstance = "HAS_INSTANCE"
  val appTierAndAPMServer = "HAS_APMSERVER"

  implicit class ApplicationTierImpl(applicationTier: ApplicationTier) extends Neo4jRep[ApplicationTier] {
    override def toNeo4jGraph(entity: ApplicationTier): Node = {
      val map = Map("name" -> entity.name,
        "description" -> entity.description)
      val node = Neo4jRepository.saveEntity[ApplicationTier](labelName, entity.id, map)
      entity.instances.map { instance =>
        val instanceNode = instance.toNeo4jGraph(instance)
        Neo4jRepository.createRelation(appTierAndInstance, node, instanceNode)
      }
      entity.apmServer.map { apmServer =>
        val apmServerNode = apmServer.toNeo4jGraph(apmServer)
        Neo4jRepository.createRelation(appTierAndAPMServer, node, apmServerNode)
      }
      node
    }

    override def fromNeo4jGraph(id: Long): Option[ApplicationTier] = {
      ApplicationTier.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[ApplicationTier] = {
    Neo4jRepository.findNodeById(id).flatMap {
      node =>
        if (Neo4jRepository.hasLabel(node, labelName)) {
          val map = Neo4jRepository.getProperties(node, "name", "description")
          val instanceNodeIds = Neo4jRepository.getChildNodeIds(id, appTierAndInstance)
          val instances = instanceNodeIds.flatMap(nodeId => Instance.fromNeo4jGraph(nodeId))
          val apmServerNodeIds = Neo4jRepository.getChildNodeIds(id, appTierAndAPMServer)
          val apmServers = apmServerNodeIds.flatMap(nodeId => APMServerDetails.fromNeo4jGraph(nodeId))
          Some(ApplicationTier(
            Some(id),
            ActiveGridUtils.getValueFromMapAs[String](map, "name"),
            ActiveGridUtils.getValueFromMapAs[String](map, "description"),
            instances,
            apmServers.headOption
          ))
        } else {
          None
        }
    }
  }
}
