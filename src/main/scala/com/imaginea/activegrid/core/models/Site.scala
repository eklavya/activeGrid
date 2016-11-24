package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory


/**
  * Created by shareefn on 27/9/16.
  */

case class Site(override val id: Option[Long], instances: List[Instance], siteName: Option[String], groupBy: Option[String]) extends BaseEntity

object Site {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def apply(id: Long): Site = {
    Site(Some(id), List.empty[Instance], None, None)
  }

  def fromNeo4jGraph(nodeId: Long): Option[Site] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "siteName", "groupBy")
        val siteName = map.get("siteName").asInstanceOf[Option[String]]
        val groupBy = map.get("groupBy").asInstanceOf[Option[String]]
        val relationship = "HAS_Instance"
        val childNodeIds: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship)
        val instances: List[Instance] = childNodeIds.flatMap { childId =>
          Instance.fromNeo4jGraph(childId)
        }
        Some(Site(Some(nodeId), instances, siteName, groupBy))
      case None =>
        logger.warn(s"could not find node for Site with nodeId $nodeId")
        None
    }
  }

  implicit class SiteImpl(site: Site) extends Neo4jRep[Site] {

    override def toNeo4jGraph(entity: Site): Node = {
      val label = "Site"
      val mapPrimitives = Map("siteName" -> entity.siteName, "groupBy" -> entity.groupBy)
      val node = Neo4jRepository.saveEntity[Site](label, entity.id, mapPrimitives)
      val relationship = "HAS_Instance"
      entity.instances.foreach { instance =>
        val instanceNode = instance.toNeo4jGraph(instance)
        Neo4jRepository.setGraphRelationship(node, instanceNode, relationship)
      }
      node
    }

    override def fromNeo4jGraph(id: Long): Option[Site] = {
      Site.fromNeo4jGraph(id)
    }
  }
}
