package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 20/12/16.
  */
case class NodeReport(override val id: Option[Long],
                      node: Instance,
                      response: String,
                      taskReports: List[TaskReport],
                      status: StepExecutionStatus) extends BaseEntity

object NodeReport {
  val labelName = "NodeReport"
  val nodeReportAndInstance = "HAS_Instance"
  val nodeReportAndTaskReport = "HAS_TaskReport"
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit class NodeReportImpl(nodeReport: NodeReport) extends Neo4jRep[NodeReport] {
    override def toNeo4jGraph(entity: NodeReport): Node = {
      val map = Map("response" -> entity.response,
        "status" -> entity.status.stepExecutionStatus)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      val instanceNode = entity.node.toNeo4jGraph(entity.node)
      Neo4jRepository.createRelation(nodeReportAndInstance, parentNode, instanceNode)
      entity.taskReports.foreach { taskReport =>
        val childNode = taskReport.toNeo4jGraph(taskReport)
        Neo4jRepository.createRelation(nodeReportAndTaskReport, parentNode, childNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[NodeReport] = {
      NodeReport.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[NodeReport] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "response", "status")
      val instanceNodeId = Neo4jRepository.getChildNodeId(id, nodeReportAndInstance)
      val mayBeInstance = instanceNodeId.flatMap(nodeId => Instance.fromNeo4jGraph(nodeId))
      val instance = mayBeInstance match {
        case Some(instanceEntity) => instanceEntity
        case None =>
          logger.warn("Instance entity is not found as it is mandatory")
          throw new Exception("Instance entity not found in NodeReport")
      }
      val reportNodeIds = Neo4jRepository.getChildNodeIds(id, nodeReportAndTaskReport)
      val taskReports = reportNodeIds.flatMap(nodeId => TaskReport.fromNeo4jGraph(nodeId))

      NodeReport(Some(id),
        instance,
        map("response").asInstanceOf[String],
        taskReports,
        StepExecutionStatus.toStepExecutionStatus(map("status").asInstanceOf[String]))
    }
  }
}
