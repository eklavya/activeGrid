package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 28/10/16.
  */
case class PolicyCondition(override val id: Option[Long],
                           applicationTier: ApplicationTier,
                           metricType: MetricType,
                           threshold: Double,
                           unitType: UnitType,
                           conditionType: ConditionType,
                           scaleType: ScaleType,
                           scalingGroup: ScalingGroup) extends BaseEntity

object PolicyCondition {

  implicit class PolicyConditionImpl(policyCondition: PolicyCondition) extends Neo4jRep[PolicyCondition] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "PolicyCondition"

    override def toNeo4jGraph(policyCondition: PolicyCondition): Node = {
      logger.debug(s"In toGraph for Software: $policyCondition")
      val map = Map("namapplicationTiere" -> policyCondition.applicationTier,
        "metricType" -> policyCondition.metricType,
        "threshold" -> policyCondition.threshold,
        "unitType" -> policyCondition.unitType,
        "conditionType" -> policyCondition.conditionType,
        "scaleType" -> policyCondition.scaleType,
        "scalingGroup" -> policyCondition.scalingGroup
      )
      val policyConditionNode = Neo4jRepository.saveEntity[PolicyCondition](label,policyCondition.id, map)
      policyConditionNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[PolicyCondition] = {
      PolicyCondition.fromNeo4jGraph(nodeId)
    }

  }

  def fromNeo4jGraph(nodeId: Long): Option[PolicyCondition] = {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    try {
      val node = Neo4jRepository.findNodeById(nodeId)
      val map = Neo4jRepository.getProperties(node.get, "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
      val policyCondition = PolicyCondition(Some(nodeId),
        map("namapplicationTiere").asInstanceOf[ApplicationTier],
        map("metricType").asInstanceOf[MetricType],
        map("threshold").asInstanceOf[Double],
        map("unitType").asInstanceOf[UnitType],
        map("conditionType").asInstanceOf[ConditionType],
        map("scaleType").asInstanceOf[ScaleType],
        map("scalingGroup").asInstanceOf[ScalingGroup])
      Some(policyCondition)
    } catch {
      case ex: Exception =>
        logger.warn(ex.getMessage, ex)
        None

    }
  }
}