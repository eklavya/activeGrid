package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 2/1/17.
  */
case class AnsiblePlay(override val id: Option[Long],
                       pattern: String,
                       taskList: List[Task],
                       content: String,
                       group: AnsibleGroup) extends BaseEntity

object AnsiblePlay {
  val labelName = "AnsiblePlay"
  val ansiblePlayAndTask = "HAS_Task"
  val ansiblePlayAndAnsibleGroup = "HAS_AnsibleGroup"

  implicit class AnsiblePlayImpl(ansiblePlay: AnsiblePlay) extends Neo4jRep[AnsiblePlay] {
    override def toNeo4jGraph(entity: AnsiblePlay): Node = {
      val map = Map("pattern" -> entity.pattern,
        "content" -> entity.content)
      val parentNode = Neo4jRepository.saveEntity[AnsiblePlay](labelName, entity.id, map)
      entity.taskList.foreach {
        task =>
          val childNode = task.toNeo4jGraph(task)
          Neo4jRepository.createRelation(ansiblePlayAndTask, parentNode, childNode)
      }
      val ansibleGroupNode = entity.group.toNeo4jGraph(entity.group)
      Neo4jRepository.createRelation(ansiblePlayAndAnsibleGroup, parentNode, ansibleGroupNode)
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[AnsiblePlay] = {
      AnsiblePlay.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[AnsiblePlay] = {
    for {
      node <- Neo4jRepository.findNodeById(id)
      ansibleGroupNodeId <- Neo4jRepository.getChildNodeId(id, ansiblePlayAndAnsibleGroup)
      group <- AnsibleGroup.fromNeo4jGraph(ansibleGroupNodeId)
    } yield {
      val map = Neo4jRepository.getProperties(node, "pattern", "content")
      val childNodeIds = Neo4jRepository.getChildNodeIds(id, ansiblePlayAndTask)
      val taskList = childNodeIds.flatMap(childId => Task.fromNeo4jGraph(childId))
      AnsiblePlay(Some(id),
        map("pattern").asInstanceOf[String],
        taskList,
        map("content").asInstanceOf[String],
        group
      )
    }
  }
}