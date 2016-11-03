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

  implicit class SiteImpl(site: Site) extends Neo4jRep[Site] {

    override def toNeo4jGraph(entity: Site): Node = {
      val label = "Site"
      val mapPrimitives = Map("siteName" -> entity.siteName.getOrElse(GraphDBExecutor.NO_VAL), "groupBy" -> entity.groupBy.getOrElse(GraphDBExecutor.NO_VAL))
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[Site](label, mapPrimitives)
      val relationship = "HAS_Instance"
      entity.instances.foreach { instance =>
        val instanceNode = instance.toNeo4jGraph(instance)
        GraphDBExecutor.setGraphRelationship(node, instanceNode, relationship)
      }
      node
    }

    override def fromNeo4jGraph(id: Long): Option[Site] = {
      Site.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[Site] = {
    val listOfKeys = List("siteName", "groupBy")
    val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
    if (propertyValues.nonEmpty) {
      val siteName = propertyValues.get("siteName").asInstanceOf[Option[String]]
      val groupBy = propertyValues.get("groupBy").asInstanceOf[Option[String]]
      val relationship = "HAS_Instance"
      val childNodeIds: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship)
      val instances: List[Instance] = childNodeIds.flatMap { childId =>
        Instance.fromNeo4jGraph(childId)
      }
      Some(Site(Some(nodeId), instances, siteName, groupBy))
    }
    else {
      logger.warn(s"could not get graph properties for Site node with $nodeId")
      None
    }
  }
}
