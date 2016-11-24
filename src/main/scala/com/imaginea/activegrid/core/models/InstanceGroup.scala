package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

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
  val instanceGroup_Instance_Relation = "HAS_INSTANCE"

  def fromNeo4jGraph(nodeId: Long): Option[InstanceGroup] = {
    Neo4jRepository.findNodeById(nodeId) match {
      case Some(node) =>
        val instances = node.getRelationships.foldLeft(List.empty[Instance]) {
          (list, relationship) =>
            Instance.fromNeo4jGraph(relationship.getEndNode.getId).get :: list
        }

        Some(InstanceGroup(Some(node.getId),
          Neo4jRepository.getProperty[String](node, "groupType"),
          Neo4jRepository.getProperty[String](node, "name"),
          instances))
      case None => None
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
          Neo4jRepository.createRelation(instanceGroup_Instance_Relation, node, childNode)
      }
      node
    }

    override def fromNeo4jGraph(nodeId: Long): Option[InstanceGroup] = {
      InstanceGroup.fromNeo4jGraph(nodeId)
    }
  }
}
