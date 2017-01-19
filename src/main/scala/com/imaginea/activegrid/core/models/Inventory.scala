package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 20/12/16.
  */
case class Inventory(override val id: Option[Long],
                     json: String,
                     siteId: Long,
                     groups: List[Group],
                     hosts: List[Host],
                     extraVariables: List[Variable]) extends BaseEntity {
  def getExtraVariableByName(varName: String): Option[Variable] = {
    extraVariables.find {
      variable => variable.name.contentEquals(varName)
    }
  }
}

object Inventory {
  val labelName = "Inventory"
  val inventoryAndGroupRelation = "HAS_Group"
  val inventoryAndHostRelation = "HAS_Host"
  val inventoryAndVariableRelation = "HAS_Variable"

  implicit class InventoryImpl(inventory: Inventory) extends Neo4jRep[Inventory] {
    override def toNeo4jGraph(entity: Inventory): Node = {
      val map = Map("json" -> entity.json,
        "siteId" -> entity.siteId)
      val parentNode = Neo4jRepository.saveEntity[Inventory](labelName, entity.id, map)
      entity.groups.foreach { group =>
        val childNode = group.toNeo4jGraph(group)
        Neo4jRepository.createRelation(inventoryAndGroupRelation, parentNode, childNode)
      }
      entity.hosts.foreach { host =>
        val childNode = host.toNeo4jGraph(host)
        Neo4jRepository.createRelation(inventoryAndHostRelation, parentNode, childNode)
      }
      entity.extraVariables.foreach { variable =>
        val childNode = variable.toNeo4jGraph(variable)
        Neo4jRepository.createRelation(inventoryAndVariableRelation, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[Inventory] = {
      Inventory.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Inventory] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "json", "siteId")
      val groupNodeIds = Neo4jRepository.getChildNodeIds(id, inventoryAndGroupRelation)
      val groups = groupNodeIds.flatMap(nodeId => Group.fromNeo4jGraph(nodeId))
      val hostNodeids = Neo4jRepository.getChildNodeIds(id, inventoryAndHostRelation)
      val hosts = hostNodeids.flatMap(nodeId => Host.fromNeo4jGraph(nodeId))
      val variableNodeIds = Neo4jRepository.getChildNodeIds(id, inventoryAndVariableRelation)
      val extraVariables = variableNodeIds.flatMap(nodeId => Variable.fromNeo4jGraph(nodeId))
      Inventory(Some(id),
        map("json").asInstanceOf[String],
        map("siteId").asInstanceOf[Long],
        groups,
        hosts,
        extraVariables)
    }
  }
}
