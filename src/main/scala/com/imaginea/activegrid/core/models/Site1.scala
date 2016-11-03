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
                 reservedInstanceDetails: List[ReservedInstanceDetails],
                 filters: List[SiteFilter],
                 loadBalancers: List[LoadBalancer],
                 scalingGroups: List[ScalingGroup],
                 groupsList: List[InstanceGroup]) extends BaseEntity

object Site1 {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val label = "Site1"
  val site_LB_Relation = "HAS_LoadBalancer"
  val site_SG_Relation = "HAS_ScalingGroup"
  val site_IG_Relation = "HAS_InstanceGroup"
  val site_RI_Relation = "HAS_ReservedInstance"
  val site_SF_Relation = "HAS_SiteFilter"

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

      val childNodeIds_sf: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, site_SF_Relation)
      val siteFilters: List[SiteFilter] = childNodeIds_sf.flatMap { childId =>
        SiteFilter.fromNeo4jGraph(childId)
      }

      val childNodeIds_lb: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, site_LB_Relation)
      val loadBalancers: List[LoadBalancer] = childNodeIds_lb.flatMap { childId =>
        LoadBalancer.fromNeo4jGraph(childId)
      }

      val childNodeIds_sg: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, site_SG_Relation)
      val scalingGroups: List[ScalingGroup] = childNodeIds_sg.flatMap { childId =>
        ScalingGroup.fromNeo4jGraph(childId)
      }

      val childNodeIds_ig: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, site_IG_Relation)
      val instanceGroups: List[InstanceGroup] = childNodeIds_ig.flatMap { childId =>
        InstanceGroup.fromNeo4jGraph(childId)
      }

      val childNodeIds_rid: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, site_RI_Relation)
      val reservedInstance: List[ReservedInstanceDetails] = childNodeIds_rid.flatMap { childId =>
        ReservedInstanceDetails.fromNeo4jGraph(childId)
      }

      Some(Site1(Some(nodeId), siteName, instances, reservedInstance, siteFilters, loadBalancers, scalingGroups, instanceGroups))
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

      entity.filters.foreach { filter =>
        val filterNode = filter.toNeo4jGraph(filter)
        GraphDBExecutor.setGraphRelationship(node, filterNode, site_SF_Relation)
      }

      entity.loadBalancers.foreach { lb =>
        val loadBalancerNode = lb.toNeo4jGraph(lb)
        GraphDBExecutor.setGraphRelationship(node, loadBalancerNode, site_LB_Relation)
      }
      entity.scalingGroups.foreach { sg =>
        val scalingGroupNode = sg.toNeo4jGraph(sg)
        GraphDBExecutor.setGraphRelationship(node, scalingGroupNode, site_SG_Relation)
      }

      entity.reservedInstanceDetails.foreach { ri =>
        val reservedInstanceNode = ri.toNeo4jGraph(ri)
        GraphDBExecutor.setGraphRelationship(node, reservedInstanceNode, site_RI_Relation)
      }

      entity.groupsList.foreach { ig =>
        val instanceGroupNode = ig.toNeo4jGraph(ig)
        GraphDBExecutor.setGraphRelationship(node, instanceGroupNode, site_IG_Relation)
      }
      node
    }

    override def fromNeo4jGraph(id: Long): Option[Site1] = {
      Site1.fromNeo4jGraph(id)
    }
  }

}