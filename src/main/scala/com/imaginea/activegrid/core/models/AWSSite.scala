package com.imaginea.activegrid.core.models
import org.neo4j.graphdb.Node

/**
  * Created by sivag on 28/10/16.
  */
class AWSSite(override val id:Option[Long],
              name:String,
              instances:Option[List[Instance]],
              filters:Option[List[SiteFilter]],
              keyPairs:Option[List[KeyPairInfo]],
              groupsList:Option[List[Instance]],
              appliacations:Option[List[ApplicationSettings]],
              groupBy:Option[String],
              loadBalancers:List[LoadBalancer],
              scalingGroups:List[ScalingGroup],
              reservedInstanceDetails: List[ReservedInstanceDetails],
              scalingPolicies:List[AutoScalingPolicy]
             ) extends BaseEntity

object AWSSite{
  implicit class AWSSiteNeo4jWrapper(aWSSite: AWSSite) extends Neo4jRep[AWSSite]{
    override def toNeo4jGraph(entity: AWSSite): Node = ??? // Need to implement
    val label = "Instance"
    override def fromNeo4jGraph(nodeId: Long): Option[AWSSite] = {
            Neo4jRepository.withTx{
              neo => {
               val instances =  Neo4jRepository.getNodesByLabel(label)

              }
            }
    }
  }
}
