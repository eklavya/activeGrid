package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class Task(override val id: Option[Long],
                name: String,
                content: String,
                taskType: TaskType,
                path: String) extends BaseEntity

object Task {
  val labelName = "Task"

  implicit class TaskImpl(task: Task) extends Neo4jRep[Task] {
    override def toNeo4jGraph(entity: Task): Node = {
      val map = Map("name" -> entity.name,
        "content" -> entity.content,
        "taskType" -> entity.taskType.taskType,
        "path" -> entity.path)
      Neo4jRepository.saveEntity(labelName, entity.id, map)
    }

    override def fromNeo4jGraph(id: Long): Option[Task] = {
      Task.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Task] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "name", "content", "taskType", "path")
      Task(Some(id),
        map("name").asInstanceOf[String],
        map("content").asInstanceOf[String],
        TaskType.toTaskType(map("taskType").asInstanceOf[String]),
        map("path").asInstanceOf[String])
    }
  }
}