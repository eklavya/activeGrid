package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by shareefn on 25/10/16.
  */
case class Site1(override val id: Option[Long],
                 siteName: String,
                 instances: List[Instance],
                 reservedInstanceDetails: List[ReservedInstanceDetails],
                 filters: List[SiteFilter],
                 loadBalancers: List[LoadBalancer],
                 scalingGroups: List[ScalingGroup],
                 groupsList: List[InstanceGroup],
                 applications: List[Application],
                 groupBy: String,
                 scalingPolicies: List[AutoScalingPolicy]) extends BaseEntity

object Site1 {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val label = "Site1"
  val site_LB_Relation = "HAS_LoadBalancer"
  val site_SG_Relation = "HAS_ScalingGroup"
  val site_IG_Relation = "HAS_InstanceGroup"
  val site_RI_Relation = "HAS_ReservedInstance"
  val site_SF_Relation = "HAS_SiteFilter"
  val siteApplications = "HAS_Applications"

  def apply(id: Long): Site1 = {
    Site1(Some(id), "test", List.empty[Instance], List.empty[ReservedInstanceDetails],
      List.empty[SiteFilter], List.empty[LoadBalancer], List.empty[ScalingGroup],
      List.empty[InstanceGroup], List.empty[Application],"test",List.empty[AutoScalingPolicy])
  }


  def delete(siteId: Long): Boolean = {
    val maybeNode = Neo4jRepository.findNodeById(siteId)
    maybeNode.map {
      node => {
        logger.info(s"Site is $siteId available,It properties are...." + node.toString)
        Neo4jRepository.deleteEntity(node.getId)
      }
    }
    maybeNode.isDefined
  }
  // scalastyle:off method.length
  def fromNeo4jGraph(nodeId: Long): Option[Site1] = {
    val mayBeNode = Neo4jRepository.findNodeById(nodeId)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "siteName")
        val siteName = map.get("siteName").toString
        val relationship_inst = "HAS_Instance"
        val groupBy = map.get("groupBy").toString
        val childNodeIds_inst: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, relationship_inst)
        val instances: List[Instance] = childNodeIds_inst.flatMap { childId =>
          Instance.fromNeo4jGraph(childId)
        }
        /*val relationship_sf = "HAS_SiteFilter"
        val childNodeIds_sf: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship_sf)
        val siteFilters: List[SiteFilter] = childNodeIds_sf.flatMap { childId =>
          SiteFilter.fromNeo4jGraph(childId)
        }*/
        val childNodeIds_sf: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, site_SF_Relation)
        val siteFilters: List[SiteFilter] = childNodeIds_sf.flatMap { childId =>
          SiteFilter.fromNeo4jGraph(childId)
        }

        val childNodeIds_lb: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, site_LB_Relation)
        val loadBalancers: List[LoadBalancer] = childNodeIds_lb.flatMap { childId =>
          LoadBalancer.fromNeo4jGraph(childId)
        }

        val childNodeIds_sg: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, site_SG_Relation)
        val scalingGroups: List[ScalingGroup] = childNodeIds_sg.flatMap { childId =>
          ScalingGroup.fromNeo4jGraph(childId)
        }

        val childNodeIds_ig: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, site_IG_Relation)
        val instanceGroups: List[InstanceGroup] = childNodeIds_ig.flatMap { childId =>
          InstanceGroup.fromNeo4jGraph(childId)
        }

        val childNodeIds_rid: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, site_RI_Relation)
        val reservedInstance: List[ReservedInstanceDetails] = childNodeIds_rid.flatMap { childId =>
          ReservedInstanceDetails.fromNeo4jGraph(childId)
        }

        val childNodeIdsApplications:List[Long] = Neo4jRepository.getChildNodeIds(nodeId, siteApplications)
        val applications: List[Application] = childNodeIdsApplications.flatMap{
          applicationId => Application.fromNeo4jGraph(applicationId)
        }


        val policies = Neo4jRepository.getChildNodeIds(nodeId, AutoScalingPolicy.relationLable).flatMap {
          id => AutoScalingPolicy.fromNeo4jGraph(id)
        }

        Some(Site1(Some(nodeId), siteName, instances, reservedInstance, siteFilters, loadBalancers,
          scalingGroups, instanceGroups, applications,groupBy, policies))
      case None =>
        logger.warn(s"could not find node for Site with nodeId $nodeId")
        None
    }
  }

  implicit class RichSite1(site1: Site1) extends Neo4jRep[Site1] {

    override def toNeo4jGraph(entity: Site1): Node = {
      val label = "Site1"
      val mapPrimitives = Map("siteName" -> entity.siteName, "groupBy" -> entity.groupBy)
      val node = Neo4jRepository.saveEntity[Site1](label, entity.id, mapPrimitives)
      val siteAndInstanceRelation = "HAS_Instance"

      entity.instances.foreach { instance =>
        Future {
          val instanceNode = instance.toNeo4jGraph(instance)
          Neo4jRepository.setGraphRelationship(node, instanceNode, siteAndInstanceRelation)
        }
      }

      entity.filters.foreach { filter =>
        val filterNode = filter.toNeo4jGraph(filter)
        Neo4jRepository.setGraphRelationship(node, filterNode, site_SF_Relation)
      }

      entity.loadBalancers.foreach { lb =>
        val loadBalancerNode = lb.toNeo4jGraph(lb)
        Neo4jRepository.setGraphRelationship(node, loadBalancerNode, site_LB_Relation)
      }
      entity.scalingGroups.foreach { sg =>
        val scalingGroupNode = sg.toNeo4jGraph(sg)
        Neo4jRepository.setGraphRelationship(node, scalingGroupNode, site_SG_Relation)
      }

      entity.reservedInstanceDetails.foreach { ri =>
        val reservedInstanceNode = ri.toNeo4jGraph(ri)
        Neo4jRepository.setGraphRelationship(node, reservedInstanceNode, site_RI_Relation)
      }

      entity.groupsList.foreach { ig =>
        val instanceGroupNode = ig.toNeo4jGraph(ig)
        Neo4jRepository.setGraphRelationship(node, instanceGroupNode, site_IG_Relation)
      }
      entity.scalingPolicies.foreach { policy =>
        val policyNode = policy.toNeo4jGraph(policy)
        Neo4jRepository.setGraphRelationship(node, policyNode, AutoScalingPolicy.relationLable)
      }

      entity.applications.foreach { application =>
        val applicationsNode = application.toNeo4jGraph(application)
        Neo4jRepository.setGraphRelationship(node, applicationsNode, siteApplications)
      }

      node
    }

    override def fromNeo4jGraph(id: Long): Option[Site1] = {
      Site1.fromNeo4jGraph(id)
    }
  }

}
