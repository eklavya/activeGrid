package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 31/10/16.
  */
case class ScalingGroup(override val id: Option[Long],
                        name: String,
                        launchConfigurationName: String,
                        status: Option[String],
                        availabilityZones: List[String],
                        instanceIds: List[String],
                        loadBalancerNames: List[String],
                        tags: List[KeyValueInfo],
                        desiredCapacity: Int,
                        maxCapacity: Int,
                        minCapacity: Int) extends BaseEntity

object ScalingGroup {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def fromNeo4jGraph(id: Long): Option[ScalingGroup] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode match {
      case Some(node) =>
        val map = Neo4jRepository.getProperties(node, "name", "launchConfigurationName", "status", "availabilityZones",
          "instanceIds", "loadBalancerNames", "desiredCapacity", "maxCapacity", "minCapacity")
        val relationship_keyValueInfo = "HAS_keyValueInfo"
        val childNodeIds_keyVal: List[Long] = Neo4jRepository.getChildNodeIds(id, relationship_keyValueInfo)
        val tags: List[KeyValueInfo] = childNodeIds_keyVal.flatMap { childId =>
          KeyValueInfo.fromNeo4jGraph(childId)
        }

        val scalingGroup = ScalingGroup(
          Some(node.getId),
          map("name").toString,
          map("launchConfigurationName").toString,
          ActiveGridUtils.getValueFromMapAs[String](map, "status"),
          map("availabilityZones").asInstanceOf[Array[String]].toList,
          map("instanceIds").asInstanceOf[Array[String]].toList,
          map("loadBalancerNames").asInstanceOf[Array[String]].toList,
          tags,
          map("desiredCapacity").toString.toInt,
          map("maxCapacity").toString.toInt,
          map("minCapacity").toString.toInt
        )
        Some(scalingGroup)
      case None => None
    }
  }

  implicit class RichScalingGroup(scalingGroup: ScalingGroup) extends Neo4jRep[ScalingGroup] {
    val label = "ScalingGroup"

    override def toNeo4jGraph(entity: ScalingGroup): Node = {
      logger.debug(s"toGraph for ScalingGroup $entity")
      val map = Map(
        "name" -> entity.name,
        "launchConfigurationName" -> entity.launchConfigurationName,
        "status" -> entity.status,
        "availabilityZones" -> entity.availabilityZones.toArray,
        "instanceIds" -> entity.instanceIds.toArray,
        "loadBalancerNames" -> entity.loadBalancerNames.toArray,
        "desiredCapacity" -> entity.desiredCapacity,
        "maxCapacity" -> entity.maxCapacity,
        "minCapacity" -> entity.minCapacity
      )
      val node = Neo4jRepository.saveEntity[ScalingGroup](label, entity.id, map)
      val relationship_keyVal = "HAS_keyValueInfo"
      entity.tags.foreach { tag =>
        val tagNode = tag.toNeo4jGraph(tag)
        Neo4jRepository.setGraphRelationship(node, tagNode, relationship_keyVal)
      }
      logger.debug(s"node - $node")
      node
    }

    override def fromNeo4jGraph(id: Long): Option[ScalingGroup] = {
      ScalingGroup.fromNeo4jGraph(id)
    }
  }

}
