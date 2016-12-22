package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class Group(override val id: Option[Long],
                 name: String,
                 instanceIds: List[String],
                 variables: List[Variable]) extends BaseEntity

object Group {
  val labelName = "Group"
  val groupAndVariableRelation = "HAS_Variable"

  implicit class GroupImpl(group: Group) extends Neo4jRep[Group] {
    override def toNeo4jGraph(entity: Group): Node = {
      val map = Map("name" -> entity.name,
        "instanceIds" -> entity.instanceIds.toArray)
      val parentNode = Neo4jRepository.saveEntity[Group](labelName, entity.id, map)
      entity.variables.foreach {
        variable =>
          val childNode = variable.toNeo4jGraph(variable)
          Neo4jRepository.createRelation(groupAndVariableRelation, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[Group] = {
      Group.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Group] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "name", "instanceIds")
      val childNodeIds = Neo4jRepository.getChildNodeIds(id, groupAndVariableRelation)
      val variables = childNodeIds.flatMap(childId => Variable.fromNeo4jGraph(childId))
      Group(Some(id),
        map("name").asInstanceOf[String],
        map("instanceIds").asInstanceOf[Array[String]].toList,
        variables)
    }
  }
}
