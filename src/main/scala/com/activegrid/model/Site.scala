package com.activegrid.model


import java.util.Date

import com.activegrid.model.Graph.Neo4jRep
import org.neo4j.graphdb.Node


/**
  * Created by shareefn on 27/9/16.
  */

case class Site(override val id: Option[Long],instances: List[Instance]) extends BaseEntity

object Site {

  implicit class SiteImpl(site: Site) extends Neo4jRep[Site] {

    override def toNeo4jGraph(entity: Site): Option[Node] = {

      val label = "Site"
      val node = GraphDBExecutor.createGraphNodeWithPrimitives[Site](label, Map.empty)
      val relationship = "HAS_site"
      entity.instances.foreach { instance =>
        val instanceNode = instance.toNeo4jGraph(instance)
        GraphDBExecutor.setGraphRelationship(node, instanceNode, relationship)
      }

      node
    }

    override def fromNeo4jGraph(id: Option[Long]): Option[Site] = {

      Site.fromNeo4jGraph(id)
    }

  }

  def fromNeo4jGraph(id: Option[Long]): Option[Site] = {

    id match {
      case Some(nodeId) => {
        val relationship = "HAS_site"
        val childNodeIds: List[Long] = GraphDBExecutor.getChildNodeIds(nodeId, relationship)
        val instances: List[Instance] = childNodeIds.map { childId =>
          Instance.fromNeo4jGraph(Some(childId)).get
        }
        Some(Site(Some(nodeId), instances))
      }
      case None => None
    }
  }

}

/*
case class Site(siteName: String,
                instances: List[Instance],
                filters: List[SiteFilter],
                keypairs: List[KeyPairInfo],
                groupsList: List[InstanceGroup],
                applications: List[Application],
                groupBy: String,
                loadBalancers: List[LoadBalancer],
                scalingGroups: List[ScalingGroup],
                reservedInstanceDetails: List[ReservedInstanceDetails],
                scalingPolicies: List[AutoScalingPolicy]



case class KeyPairStatus()




*/