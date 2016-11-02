package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by sivag on 28/10/16.
  */
case class   AWSSite(override val id: Option[Long],
              name: String,
              instances: Option[List[Instance]],
              filters: Option[List[SiteFilter]],
              keyPairs: Option[List[KeyPairInfo]],
              groupsList: Option[List[Instance]],
              appliacations: Option[List[Application]],
              groupBy: Option[String],
              loadBalancers: List[LoadBalancer],
              scalingGroups: List[ScalingGroup],
              reservedInstanceDetails: List[ReservedInstanceDetails],
              scalingPolicies: List[AutoScalingPolicy]
             ) extends BaseEntity

object AWSSite {
  implicit class AWSSiteNeo4jWrapper(aWSSite: Option[AWSSite]) extends Neo4jRep[AWSSite] {
    override def toNeo4jGraph(entity: AWSSite): Node = ???
    // Need to implement
    val label = "AWSSite"

    override def fromNeo4jGraph(nodeId: Long): Option[AWSSite] = {
      Neo4jRepository.withTx {
        neo => {
         Some(Neo4jRepository.findNodeById(nodeId).asInstanceOf[AWSSite])
        }
      }
    }
  }
}
