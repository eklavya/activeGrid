package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class Host(override val id: Option[Long],
                instanceId: String,
                variables: List[Variable]) extends BaseEntity

object Host {
  val labelName = "Host"
  val hostAndVariableRelation = "HAS_Variable"

  implicit class HostImpl(host: Host) extends Neo4jRep[Host] {
    override def toNeo4jGraph(entity: Host): Node = {
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, Map("instanceId" -> entity.instanceId))
      entity.variables.foreach { variable =>
        val childNode = variable.toNeo4jGraph(variable)
        Neo4jRepository.createRelation(hostAndVariableRelation, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[Host] = {
      Host.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Host] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map {
      node =>
        val childNodeIds = Neo4jRepository.getChildNodeIds(id, hostAndVariableRelation)
        val variables = childNodeIds.flatMap(childNodeId => Variable.fromNeo4jGraph(childNodeId))
        Host(Some(id), node.getProperty("instanceId").asInstanceOf[String], variables)
    }
  }
}