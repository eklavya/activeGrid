package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 25/10/16.
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
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "siteName")
        val siteName = map.get("siteName").toString
        val relationship_inst = "HAS_Instance"
        val childNodeIds_inst: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship_inst)
        val instances: List[Instance] = childNodeIds_inst.flatMap { childId =>
          Instance.fromNeo4jGraph(childId)
        }
        /*val relationship_sf = "HAS_SiteFilter"
        val childNodeIds_sf: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_sf)
        val siteFilters: List[SiteFilter] = childNodeIds_sf.flatMap { childId =>
          SiteFilter.fromNeo4jGraph(childId)
        }*/
        val relationship_lb = "HAS_LoadBalancer"
        val childNodeIds_lb: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship_lb)
        val loadBalancers: List[LoadBalancer] = childNodeIds_lb.flatMap { childId =>
          LoadBalancer.fromNeo4jGraph(childId)
        }
        val relationship_sg = "HAS_ScalingGroup"
        val childNodeIds_sg: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship_sg)
        val scalingGroups: List[ScalingGroup] = childNodeIds_sg.flatMap { childId =>
          ScalingGroup.fromNeo4jGraph(childId)
        }

        Some(Site1(Some(nodeId), siteName, instances, List.empty[SiteFilter], loadBalancers, scalingGroups))
      case None =>
        logger.warn(s"could not find node for Site with nodeId $nodeId")
        None
    }
  }

  implicit class RichSite1(site1: Site1) extends Neo4jRep[Site1] {

    override def toNeo4jGraph(entity: Site1): Node = {
      val label = "Site1"
      val mapPrimitives = Map("siteName" -> entity.siteName)
      val node = Neo4jRepository.saveEntity[Site1](label, entity.id, mapPrimitives)
      val relationship_inst = "HAS_Instance"
      entity.instances.foreach { instance =>
        val instanceNode = instance.toNeo4jGraph(instance)
        Neo4jRepository.setGraphRelationship(node, instanceNode, relationship_inst)
      }
      /*val relationship = "HAS_SiteFilter"
      entity.filters.foreach { filter =>
        val filterNode = filter.toNeo4jGraph(filter)
        GraphDBExecutor.setGraphRelationship(node, filterNode, relationship)
      }*/
      val relationship_lb = "HAS_LoadBalancer"
      entity.loadBalancers.foreach { lb =>
        val loadBalancerNode = lb.toNeo4jGraph(lb)
        Neo4jRepository.setGraphRelationship(node, loadBalancerNode, relationship_lb)
      }
      val relationship_sg = "HAS_ScalingGroup"
      entity.scalingGroups.foreach { sg =>
        val scalingGroupNode = sg.toNeo4jGraph(sg)
        Neo4jRepository.setGraphRelationship(node, scalingGroupNode, relationship_sg)
      }
      node
    }

    override def fromNeo4jGraph(id: Long): Option[Site1] = {
      Site1.fromNeo4jGraph(id)
    }
  }

}
