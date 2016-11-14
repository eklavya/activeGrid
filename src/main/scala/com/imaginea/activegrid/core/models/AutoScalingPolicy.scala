package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 28/10/16.
  */
case class AutoScalingPolicy(override val id: Option[Long],
                             application: Application,
                             primaryConditions: List[PolicyCondition],
                             secondaryConditions: List[PolicyCondition],
                             lastAppliedAt: Double) extends BaseEntity

object AutoScalingPolicy {

  implicit class AutoScalingPolicyImpl(autoScalingPolicy: AutoScalingPolicy) extends Neo4jRep[AutoScalingPolicy] {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    val label = "AutoScalingPolicy"

    override def toNeo4jGraph(autoScalingPolicy: AutoScalingPolicy): Node = {
      logger.debug(s"In toGraph for Software: $autoScalingPolicy")
      val map = Map("application" -> autoScalingPolicy.application,
        "primaryConditions" -> autoScalingPolicy.primaryConditions,
        "secondaryConditions" -> autoScalingPolicy.secondaryConditions,
        "lastAppliedAt" -> autoScalingPolicy.lastAppliedAt)
      val autoScalingPolicyNode = GraphDBExecutor.saveEntity[PolicyCondition](label, map)
      autoScalingPolicyNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[AutoScalingPolicy] = {
      AutoScalingPolicy.fromNeo4jGraph(nodeId)
    }

  }

  def fromNeo4jGraph(nodeId: Long): Option[AutoScalingPolicy] = {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    try {
      val node = GraphDBExecutor.findNodeById(nodeId)
      val map = GraphDBExecutor.getProperties(node.get, "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
      val autoScalingPolicy = AutoScalingPolicy(Some(nodeId),
        map("application").asInstanceOf[Application],
        map("primaryConditions").asInstanceOf[Array[PolicyCondition]].toList,
        map("secondaryConditions").asInstanceOf[Array[PolicyCondition]].toList,
        map("lastAppliedAt").asInstanceOf[Double])
      Some(autoScalingPolicy)
    } catch {
      case ex: Exception =>
        logger.warn(ex.getMessage, ex)
        None

    }
  }
}