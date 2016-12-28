package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 21/12/16.
  */
case class AnsibleModuleDefinition(override val id: Option[Long],
                                   playBooks: List[ScriptFile],
                                   roles: List[ScriptFile]) extends ModuleDefinition

object AnsibleModuleDefinition {
  val labelName = "AnsibleModuleDefinition"
  val ansibleAndPlayBook = "HAS_ScriptFileAsPlayBook"
  val ansibleAndRole = "HAS_ScriptFileAsRole"

  implicit class AnsibleModuleDefinitionImpl(ansibleModuleDefinition: AnsibleModuleDefinition) extends Neo4jRep[AnsibleModuleDefinition] {
    override def toNeo4jGraph(entity: AnsibleModuleDefinition): Node = {
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, Map.empty[String, Any])
      entity.playBooks.foreach { playBook =>
        val childNode = playBook.toNeo4jGraph(playBook)
        Neo4jRepository.createRelation(ansibleAndPlayBook, parentNode, childNode)
      }
      entity.roles.foreach { role =>
        val childNode = role.toNeo4jGraph(role)
        Neo4jRepository.createRelation(ansibleAndRole, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[AnsibleModuleDefinition] = {
      AnsibleModuleDefinition.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[AnsibleModuleDefinition] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val playBookeNodeIds = Neo4jRepository.getChildNodeIds(id, ansibleAndPlayBook)
      val playBooks = playBookeNodeIds.flatMap(nodeId => ScriptFile.fromNeo4jGraph(nodeId))
      val roleNodeIds = Neo4jRepository.getChildNodeIds(id, ansibleAndRole)
      val roles = roleNodeIds.flatMap(nodeId => ScriptFile.fromNeo4jGraph(nodeId))
      AnsibleModuleDefinition(Some(id), playBooks, roles)
    }
  }

}