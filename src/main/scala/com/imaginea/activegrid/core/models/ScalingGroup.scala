package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by sampathr on 28/10/16.
  */
case class ScalingGroup(override val id: Option[Long],
                        name: Option[String],
                        launchConfigurationName: Option[String],
                        status: Option[String],
                        availabilityZones: List[String],
                        instanceIds: List[String],
                        loadBalancerNames: List[String],
                        tags: List[KeyValueInfo],
                        desiredCapacity: Long,
                        maxCapacity: Long,
                        minCapacity: Long) extends BaseEntity

object ScalingGroup {

  implicit class ScalinggroupImpl(scalingGroup: ScalingGroup) extends Neo4jRep[ScalingGroup] {

    val logger = Logger(LoggerFactory.getLogger(getClass.getName))

    override def toNeo4jGraph(scalingGroup: ScalingGroup): Node = {
      logger.debug(s"In toGraph for ScalingGroup: $scalingGroup")
      val label = "ScalingGroup"
      val map = Map("name" -> scalingGroup.name,
        "launchConfigurationName" -> scalingGroup.launchConfigurationName,
        "status" -> scalingGroup.status,
        "availabilityZones" -> scalingGroup.availabilityZones.toArray,
        "instanceIds" -> scalingGroup.instanceIds.toArray,
        "loadBalancerNames" -> scalingGroup.loadBalancerNames.toArray,
        "tags" -> scalingGroup.tags.toArray,
        "desiredCapacity" -> scalingGroup.desiredCapacity,
        "maxCapacity" -> scalingGroup.maxCapacity,
        "minCapacity" -> scalingGroup.minCapacity)
      val sclaingGroupNode = GraphDBExecutor.saveEntity[ScalingGroup](label, map)
      sclaingGroupNode
    }

    override def fromNeo4jGraph(nodeId: Long): Option[ScalingGroup] = {
      None
    }


  }

  def fromNeo4jGraph(nodeId: Long): Option[ScalingGroup] = {
    val logger = Logger(LoggerFactory.getLogger(getClass.getName))
    try {
      val node = GraphDBExecutor.findNodeById(nodeId)
      val map = GraphDBExecutor.getProperties(node.get, "version", "name", "provider", "downloadURL", "port", "processNames", "discoverApplications")
      val scalingGroup = ScalingGroup(Some(nodeId),
        ActiveGridUtils.getValueFromMapAs[String](map, "name"),
        ActiveGridUtils.getValueFromMapAs[String](map, "launchConfigurationName"),
        ActiveGridUtils.getValueFromMapAs[String](map, "status"),
        map("availabilityZones").asInstanceOf[Array[String]].toList,
        map("instanceIds").asInstanceOf[Array[String]].toList,
        map("loadBalancerNames").asInstanceOf[Array[String]].toList,
        map("tags").asInstanceOf[Array[KeyValueInfo]].toList,
        map("desiredCapacity").asInstanceOf[Long],
        map("maxCapacity").asInstanceOf[Long],
        map("minCapacity").asInstanceOf[Long])
      Some(scalingGroup)
    } catch {
      case ex: Exception =>
        logger.warn(ex.getMessage, ex)
        None

    }
  }

}