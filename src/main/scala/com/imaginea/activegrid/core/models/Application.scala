package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by nagulmeeras on 21/11/16.
  */
case class Application(override val id: Option[Long],
                       name: Option[String],
                       description: Option[String],
                       version: Option[String],
                       instances: List[Instance],
                       software: Option[Software],
                       tiers: List[ApplicationTier],
                       apmServer: Option[APMServerDetails],
                       responseTime: Option[Double]) extends BaseEntity

object Application {

  val label = Application.getClass.getSimpleName
  val relationLabel = ActiveGridUtils.relationLbl(label)
  val logger = LoggerFactory.getLogger(getClass)

  implicit class ApplicationImpl(application: Application) extends Neo4jRep[Application] {

    override def toNeo4jGraph(entity: Application): Node = {

      //Saving node and properties
      val map = Map("name" -> entity.name, "description" -> entity.description, "version" -> entity.version,
        "responseTime" -> entity.responseTime)
      val node = Neo4jRepository.saveEntity[Application](Application.label, entity.id, map)

      //Mapping instance to application
      entity.instances.foreach { instance =>
        val instanceNode = instance.toNeo4jGraph(instance)
        Neo4jRepository.createRelation(Instance.relationLabel, node, instanceNode)
      }
      //Mapping software to application
      entity.software.foreach { software =>
        val softwareNode = software.toNeo4jGraph(software)
        Neo4jRepository.createRelation(Software.relationLabel, node, softwareNode)
      }
      //Mapping ApplicationTier
      entity.tiers.foreach { appTier =>
        val tierNode = appTier.toNeo4jGraph(appTier)
        Neo4jRepository.createRelation(ApplicationTier.relationLabel, node, tierNode)
      }

      //Mappoing APM server details
      entity.apmServer.foreach { apmServer =>
        val serverNode = apmServer.toNeo4jGraph(apmServer)
        Neo4jRepository.createRelation(APMServerDetails.relationLabel, node, serverNode)
      }
      node
    }

    override def fromNeo4jGraph(id: Long): Option[Application] = {
      Application.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Application] = {
    Neo4jRepository.findNodeById(id).flatMap {
      node =>
        if (Neo4jRepository.hasLabel(node, Application.label)) {
          // Fetching properties
          val map = Neo4jRepository.getProperties(node, "name", "description", "version", "responseTime")
          val instanceNodeIds = Neo4jRepository.getChildNodeIds(id, Instance.relationLabel)
          val instances = instanceNodeIds.flatMap(nodeId => Instance.fromNeo4jGraph(nodeId))
          val softwareNodeids = Neo4jRepository.getChildNodeIds(id, Software.relationLabel)
          val software = softwareNodeids.flatMap(nodeId => Software.fromNeo4jGraph(nodeId))
          val appTierNodeIds = Neo4jRepository.getChildNodeIds(id, ApplicationTier.relationLabel)
          val appTiers = appTierNodeIds.flatMap(nodeId => ApplicationTier.fromNeo4jGraph(nodeId))
          val aPMServerDetailsNodeIds = Neo4jRepository.getChildNodeIds(id, APMServerDetails.relationLabel)
          val aPMServerDetails = aPMServerDetailsNodeIds.flatMap(nodeId => APMServerDetails.fromNeo4jGraph(nodeId))
          // Creating application instance
          Some(Application(Some(node.getId),
            ActiveGridUtils.getValueFromMapAs[String](map, "name"),
            ActiveGridUtils.getValueFromMapAs[String](map, "description"),
            ActiveGridUtils.getValueFromMapAs[String](map, "version"),
            instances,
            software.headOption,
            appTiers,
            aPMServerDetails.headOption,
            ActiveGridUtils.getValueFromMapAs[Double](map, "responseTime")))
        } else {
          None
        }
    }
  }
}