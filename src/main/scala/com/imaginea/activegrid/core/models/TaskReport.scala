package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 20/12/16.
  */
case class TaskReport(override val id: Option[Long],
                      task: Task,
                      status: TaskStatus,
                      result: String) extends BaseEntity

object TaskReport {
  val labelName = "TaskReport"
  val taskReportAndTask = "HAS_Task"
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit class TaskReportImpl(taskReport: TaskReport) extends Neo4jRep[TaskReport] {
    override def toNeo4jGraph(entity: TaskReport): Node = {
      val map = Map("status" -> entity.status.taskStatus,
        "result" -> entity.result)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      val taskNode = entity.task.toNeo4jGraph(entity.task)
      Neo4jRepository.createRelation(taskReportAndTask, parentNode, taskNode)
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[TaskReport] = {
      TaskReport.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[TaskReport] = {
    for {
      node <- Neo4jRepository.findNodeById(id)
      taskNodeId <- Neo4jRepository.getChildNodeId(id, taskReportAndTask)
      task <- Task.fromNeo4jGraph(taskNodeId)
    } yield {
      val map = Neo4jRepository.getProperties(node, "status", "result")
      TaskReport(Some(id),
        task,
        TaskStatus.toTaskStatus(map("status").asInstanceOf[String]),
        map("result").asInstanceOf[String])
    }
  }
}