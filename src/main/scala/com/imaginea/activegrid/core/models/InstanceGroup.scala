package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

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
  val instanceGroupInstanceRelation = "HAS_INSTANCE"

  implicit class InstanceGroupImpl(instanceGroup: InstanceGroup) extends Neo4jRep[InstanceGroup] {
    override def toNeo4jGraph(entity: InstanceGroup): Node = {
      logger.debug(s"Executing $getClass :: toNeo4jGraph ")
      val map: Map[String, Any] = Map("groupType" -> entity.groupType,
        "name" -> entity.name)
      val node = Neo4jRepository.saveEntity[InstanceGroup](instanceGroupLabel, entity.id, map)
      entity.instances.foreach {
        instance =>
          val childNode = instance.toNeo4jGraph(instance)
          Neo4jRepository.createRelation(instanceGroupInstanceRelation, node, childNode)
      }
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[InstanceGroup] = {
      InstanceGroup.fromNeo4jGraph(nodeId)
    }
  }

  def fromNeo4jGraph(nodeId: Long): Option[InstanceGroup] = {
    Neo4jRepository.findNodeById(nodeId) match {
      case Some(node) =>

        val childNodeIdsig: List[Long] = Neo4jRepository.getChildNodeIds(nodeId, instanceGroupInstanceRelation)
        val instances: List[Instance] = childNodeIdsig.flatMap { childId =>
          Instance.fromNeo4jGraph(childId)
        }

        Some(InstanceGroup(Some(node.getId),
          Neo4jRepository.getProperty[String](node, "groupType"),
          Neo4jRepository.getProperty[String](node, "name"),
          instances))
      case None => None
    }
  }
}
