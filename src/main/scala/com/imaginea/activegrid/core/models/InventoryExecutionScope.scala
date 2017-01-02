package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class InventoryExecutionScope(override val id: Option[Long],
                                   json: String,
                                   siteId: Long,
                                   groups: List[Group],
                                   hosts: List[Host],
                                   extraVariables: List[Variable]) extends BaseEntity

object InventoryExecutionScope {
  val labelName = "InventoryExecutionScope"
  val inventoryAndGroup = "HAS_Group"
  val inventoryAndHost = "HAS_Host"
  val inventoryAndVariable = "HAS_Variable"

  implicit class InventoryExecutionScopeImpl(inventoryExecutionScope: InventoryExecutionScope) extends Neo4jRep[InventoryExecutionScope] {
    override def toNeo4jGraph(entity: InventoryExecutionScope): Node = {
      val map = Map("json" -> entity.json,
        "siteId" -> entity.siteId)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      entity.groups.foreach { group =>
        val groupNode = group.toNeo4jGraph(group)
        Neo4jRepository.createRelation(inventoryAndGroup, parentNode, groupNode)
      }
      entity.hosts.foreach { host =>
        val hostNode = host.toNeo4jGraph(host)
        Neo4jRepository.createRelation(inventoryAndHost, parentNode, hostNode)
      }
      entity.extraVariables.foreach { variable =>
        val variableNode = variable.toNeo4jGraph(variable)
        Neo4jRepository.createRelation(inventoryAndVariable, parentNode, variableNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[InventoryExecutionScope] = {
      InventoryExecutionScope.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[InventoryExecutionScope] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "json", "siteId")
      val groupNodeId = Neo4jRepository.getChildNodeIds(id, inventoryAndGroup)
      val groups = groupNodeId.flatMap(nodeId => Group.fromNeo4jGraph(nodeId))
      val hostNodeId = Neo4jRepository.getChildNodeIds(id, inventoryAndHost)
      val hosts = hostNodeId.flatMap(nodeId => Host.fromNeo4jGraph(nodeId))
      val variableNodeId = Neo4jRepository.getChildNodeIds(id, inventoryAndVariable)
      val variables = variableNodeId.flatMap(nodeId => Variable.fromNeo4jGraph(nodeId))
      InventoryExecutionScope(Some(id),
        map("json").asInstanceOf[String],
        map("siteId").asInstanceOf[Long],
        groups,
        hosts,
        variables)
    }
  }

}
