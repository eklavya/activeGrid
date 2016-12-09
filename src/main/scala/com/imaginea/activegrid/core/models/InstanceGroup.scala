package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

//scalastyle:ignore underscore.import

/**
  * Created by nagulmeeras on 01/11/16.
  */
case class InstanceGroup(override val id: Option[Long],
                         groupType: Option[String],
                         name: Option[String],
                         instances: List[Instance]) extends BaseEntity

object InstanceGroup {
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))
  val instanceGroupLabel = "InstanceGroup"
  val instanceGroupAndInstanceRelation = "HAS_INSTANCE"

  def fromNeo4jGraph(nodeId: Long): Option[InstanceGroup] = {
    Neo4jRepository.findNodeById(nodeId).flatMap {
      node =>
        if (Neo4jRepository.hasLabel(node, instanceGroupLabel)) {
          val childNodeIds: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, instanceGroupAndInstanceRelation)
          val instances: List[Instance] = childNodeIds.flatMap { childId =>
            Instance.fromNeo4jGraph(childId)
          }

          Some(InstanceGroup(Some(node.getId),
            Neo4jRepository.getProperty[String](node, "groupType"),
            Neo4jRepository.getProperty[String](node, "name"),
            instances))
        } else {
          None
        }
    }
  }

  implicit class InstanceGroupImpl(instanceGroup: InstanceGroup) extends Neo4jRep[InstanceGroup] {
    override def toNeo4jGraph(entity: InstanceGroup): Node = {
      logger.debug(s"Executing $getClass :: toNeo4jGraph ")
      val map: Map[String, Any] = Map("groupType" -> entity.groupType,
        "name" -> entity.name)
      val node = Neo4jRepository.saveEntity[InstanceGroup](instanceGroupLabel, entity.id, map)
      entity.instances.foreach {
        instance =>
          val childNode = instance.toNeo4jGraph(instance)
          Neo4jRepository.createRelation(instanceGroupAndInstanceRelation, node, childNode)
      }
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[InstanceGroup] = {
      InstanceGroup.fromNeo4jGraph(nodeId)
    }
  }

}
