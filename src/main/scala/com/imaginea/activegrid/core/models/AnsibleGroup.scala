package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 2/1/17.
  */
case class AnsibleGroup(override val id: Option[Long],
                        name: Option[String],
                        variables: List[Variable]) extends BaseEntity

object AnsibleGroup {
  val labelName = "AnsibleGroup"
  val ansibleGroupAndVariable = "HAS_Variable"

  implicit class AnsibleGroupImpl(ansibleGroup: AnsibleGroup) extends Neo4jRep[AnsibleGroup] {
    override def toNeo4jGraph(entity: AnsibleGroup): Node = {
      val map = Map("name" -> entity.name)
      val parentNode = Neo4jRepository.saveEntity[AnsibleGroup](labelName, entity.id, map)
      entity.variables.foreach {
        variable =>
          val childNode = variable.toNeo4jGraph(variable)
          Neo4jRepository.createRelation(ansibleGroupAndVariable, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[AnsibleGroup] = {
      AnsibleGroup.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[AnsibleGroup] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "name")
      val childNodeIds = Neo4jRepository.getChildNodeIds(id, ansibleGroupAndVariable)
      val variables = childNodeIds.flatMap(childId => Variable.fromNeo4jGraph(childId))
      AnsibleGroup(Some(id),
        map.get("name").asInstanceOf[Option[String]],
        variables
      )
    }
  }
}
