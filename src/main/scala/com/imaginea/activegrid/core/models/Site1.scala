
package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by nagulmeeras on 25/10/16.
  */

case class Site1(override val id: Option[Long],
                 siteName: String,
                 instances: List[Instance],
                 filters: List[SiteFilter],
                 loadBalancers: List[LoadBalancer],
                 scalingGroups: List[ScalingGroup]) extends BaseEntity

object Site1 {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val label = "Site1"

  def fromNeo4jGraph(nodeId: Long): Option[Site1] = {
    val listOfKeys = List("siteName")
    val propertyValues = GraphDBExecutor.getGraphProperties(nodeId, listOfKeys)
    if (propertyValues.nonEmpty) {
      val siteName = propertyValues.get("siteName").toString
      val relationship_inst = "HAS_Instance"
      val childNodeIds_inst: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_inst)
      val instances: List[Instance] = childNodeIds_inst.flatMap { childId =>
        Instance.fromNeo4jGraph(childId)
      }
      /*val relationship_sf = "HAS_SiteFilter"
      val childNodeIds_sf: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_sf)
      val siteFilters: List[SiteFilter] = childNodeIds_sf.flatMap { childId =>
        SiteFilter.fromNeo4jGraph(childId)
      }*/
      val relationship_lb = "HAS_LoadBalancer"
      val childNodeIds_lb: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_lb)
      val loadBalancers: List[LoadBalancer] = childNodeIds_lb.flatMap { childId =>
        LoadBalancer.fromNeo4jGraph(childId)
      }
      val relationship_sg = "HAS_ScalingGroup"
      val childNodeIds_sg: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_sg)
      val scalingGroups: List[ScalingGroup] = childNodeIds_sg.flatMap { childId =>
        ScalingGroup.fromNeo4jGraph(childId)
      }
      Some(Site1(Some(nodeId), siteName, instances, List.empty[SiteFilter], loadBalancers, scalingGroups))
    }
    else {
      logger.warn(s"could not get graph properties for Site node with $nodeId")
      None
    }
  }

  implicit class RichSite1(site1: Site1) extends Neo4jRep[Site1] {

    override def toNeo4jGraph(entity: Site1): Node = {
      val label = "Site1"
      val mapPrimitives = Map("siteName" -> entity.siteName)
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[Site1](label, mapPrimitives)
      val relationship_inst = "HAS_Instance"
      entity.instances.foreach { instance =>
        val instanceNode = instance.toNeo4jGraph(instance)
        GraphDBExecutor.setGraphRelationship(node, instanceNode, relationship_inst)
      }
      /*val relationship = "HAS_SiteFilter"
      entity.filters.foreach { filter =>
        val filterNode = filter.toNeo4jGraph(filter)
        GraphDBExecutor.setGraphRelationship(node, filterNode, relationship)
      }*/
      val relationship_lb = "HAS_LoadBalancer"
      entity.loadBalancers.foreach { lb =>
        val loadBalancerNode = lb.toNeo4jGraph(lb)
        GraphDBExecutor.setGraphRelationship(node, loadBalancerNode, relationship_lb)
      }
      val relationship_sg = "HAS_ScalingGroup"
      entity.scalingGroups.foreach { sg =>
        val scalingGroupNode = sg.toNeo4jGraph(sg)
        GraphDBExecutor.setGraphRelationship(node, scalingGroupNode, relationship_sg)
      }
      node
    }

    override def fromNeo4jGraph(id: Long): Option[Site1] = {
      Site1.fromNeo4jGraph(id)
    }
  }

}