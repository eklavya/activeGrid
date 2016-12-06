package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.models.{ApplicationTier => AppTier, Neo4jRepository => Neo}
import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 23/11/16.
  */
case class ApplicationTier(override val id: Option[Long],
                           name: String,
                           description: String,
                           instances: List[Instance],
                           apmServer: Option[APMServerDetails]) extends BaseEntity

object ApplicationTier {

  val label = ApplicationTier.getClass.getSimpleName
  val relationLabel = ActiveGridUtils.relationLbl(label)

  def fromNeo4jGraph(nodeId: Long): Option[AppTier] = {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val node = Neo.findNodeById(nodeId)
    node.map {
      appTier =>
        //Reading properties
        val map = Neo.getProperties(appTier, "name", "description")

        //Fetching instances
        val instances = Neo.getChildNodeIds(appTier.getId, Instance.relationLabel).flatMap {
          id => Instance.fromNeo4jGraph(id)
        }

        // Fetching APM Server
        val apmSrvr = Neo.getChildNodeId(appTier.getId, APMServerDetails.relationLabel).flatMap {
          id => APMServerDetails.fromNeo4jGraph(id)
        }
        ApplicationTier(Some(nodeId), map("name").toString, map("description").toString, instances, apmSrvr)
    }
  }

  implicit class ApplicationTierImpl(applicationTier: AppTier) extends Neo4jRep[AppTier] {

    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    override def toNeo4jGraph(appTier: AppTier): Node = {

      logger.debug(s"In toGraph for Software: $appTier")

      val map = Map("name" -> appTier.name, "description" -> appTier.description)
      val appTierNode = Neo.saveEntity[AppTier](AppTier.label, appTier.id, map)

      // Creating instances.
      appTier.instances.map {
        instance =>
          val instnNode = instance.toNeo4jGraph(instance)
          Neo.createRelation(Instance.relationLabel, appTierNode, instnNode)
      }

      // Creating APMServer
      appTier.apmServer.map {
        srvr =>
          val srvrNod = srvr.toNeo4jGraph(srvr)
          Neo.createRelation(APMServerDetails.relationLabel, appTierNode, srvrNod)
      }
      appTierNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[AppTier] = {
      AppTier.fromNeo4jGraph(nodeId)
    }

  }

}