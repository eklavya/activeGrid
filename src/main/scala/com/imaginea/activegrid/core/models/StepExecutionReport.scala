package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by shareefn on 20/12/16.
  */
case class StepExecutionReport(override val id: Option[Long],
                               step: Option[Step],
                               nodeReports: List[NodeReport],
                               status: CumulativeStepExecutionStatus) extends BaseEntity

object StepExecutionReport {
  val labelName = "StepExecutionReport"
  val stepExecAndStep = "HAS_Step"
  val stepAndNodeReport = "HAS_NodeReport"

  implicit class StepExecutionReportImpl(stepExecutionReport: StepExecutionReport) extends Neo4jRep[StepExecutionReport] {
    override def toNeo4jGraph(entity: StepExecutionReport): Node = {
      val map = Map("status" -> entity.status.cumulativeStepExecutionStatus)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      entity.step.foreach { step =>
        val stepNode = step.toNeo4jGraph(step)
        Neo4jRepository.createRelation(stepExecAndStep, parentNode, stepNode)
      }
      entity.nodeReports.foreach { nodeReport =>
        val reportNode = nodeReport.toNeo4jGraph(nodeReport)
        Neo4jRepository.createRelation(stepAndNodeReport, parentNode, reportNode)
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[StepExecutionReport] = {
      StepExecutionReport.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[StepExecutionReport] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "status")
      val stepNodeId = Neo4jRepository.getChildNodeId(id, stepExecAndStep)
      val step = stepNodeId.flatMap(nodeId => Step.fromNeo4jGraph(nodeId))
      val reportNodeIds = Neo4jRepository.getChildNodeIds(id, stepAndNodeReport)
      val reports = reportNodeIds.flatMap(nodeId => NodeReport.fromNeo4jGraph(nodeId))
      StepExecutionReport(Some(id),
        step,
        reports,
        CumulativeStepExecutionStatus.toCumulativeStepExecutionStatus(map("status").asInstanceOf[String]))
    }
  }
}
