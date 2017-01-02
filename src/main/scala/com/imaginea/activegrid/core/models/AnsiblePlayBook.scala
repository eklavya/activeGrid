package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 2/1/17.
  */
case class AnsiblePlayBook(override val id: Option[Long],
                           name: String,
                           path: String,
                           playList: List[AnsiblePlay],
                           variables: List[Variable]) extends BaseEntity

object AnsiblePlayBook {
  val labelName = "AnsiblePlayBook"
  val playBookAndPlay = "HAS_AnsiblePlay"
  val playBookAndVariable = "HAS_Variable"

  implicit class AnsiblePlayBookImpl(ansiblePlayBook: AnsiblePlayBook) extends Neo4jRep[AnsiblePlayBook] {
    override def toNeo4jGraph(entity: AnsiblePlayBook): Node = {
      val map = Map("name" -> entity.name,
        "path" -> entity.path)
      val parentNode = Neo4jRepository.saveEntity[AnsiblePlayBook](labelName, entity.id, map)
      entity.playList.foreach {
        play =>
          val childNode = play.toNeo4jGraph(play)
          Neo4jRepository.createRelation(playBookAndPlay, parentNode, childNode)
      }
      entity.variables.foreach {
        variable =>
          val childNode = variable.toNeo4jGraph(variable)
          Neo4jRepository.createRelation(playBookAndVariable, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[AnsiblePlayBook] = {
      AnsiblePlayBook.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[AnsiblePlayBook] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "name", "path")
      val childNodeIds = Neo4jRepository.getChildNodeIds(id, playBookAndPlay)
      val playList = childNodeIds.flatMap(childId => AnsiblePlay.fromNeo4jGraph(childId))
      val childNodeIdsVariables = Neo4jRepository.getChildNodeIds(id, playBookAndVariable)
      val variables = childNodeIdsVariables.flatMap(childId => Variable.fromNeo4jGraph(childId))
      AnsiblePlayBook(Some(id),
        map("name").asInstanceOf[String],
        map("instanceIds").asInstanceOf[String],
        playList,
        variables)
    }
  }
}