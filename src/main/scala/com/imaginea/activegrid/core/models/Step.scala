package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 19/12/16.
  */
case class Step(override val id: Option[Long],
                stepId: String,
                name: String,
                description: String,
                stepType: StepType,
                scriptDefinition: ScriptDefinition,
                input: StepInput,
                scope: InventoryExecutionScope,
                executionOrder: Int,
                childStep: List[Step],
                report: StepExecutionReport) extends BaseEntity

object Step {
  val labelName = "Step"
  val stepAndScriptDef = "HAS_ScriptDefinition"
  val stepAndStepInput = "HAS_StepInput"
  val stepAndInventoryExec = "HAS_InvExecScope"
  val stepAndStep = "HAS_Step"
  val stepAndReport = "HAS_Report"
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  implicit class StepImpl(step: Step) extends Neo4jRep[Step] {
    override def toNeo4jGraph(entity: Step): Node = {
      val map = Map("stepId" -> entity.stepId,
        "name" -> entity.name,
        "description" -> entity.description,
        "stepType" -> entity.stepType.stepType,
        "executionOrder" -> entity.executionOrder)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      val scriptDefNode = entity.scriptDefinition.toNeo4jGraph(entity.scriptDefinition)
      Neo4jRepository.createRelation(stepAndScriptDef, parentNode, scriptDefNode)
      val stepInputNode = entity.input.toNeo4jGraph(entity.input)
      Neo4jRepository.createRelation(stepAndStepInput, parentNode, stepInputNode)
      val invScopeNode = entity.scope.toNeo4jGraph(entity.scope)
      Neo4jRepository.createRelation(stepAndInventoryExec, parentNode, invScopeNode)
      entity.childStep.foreach { step =>
        val stepNode = step.toNeo4jGraph(step)
        Neo4jRepository.createRelation(stepAndStep, parentNode, stepNode)
      }
      val reportNode = entity.report.toNeo4jGraph(entity.report)
      Neo4jRepository.createRelation(stepAndReport, parentNode, reportNode)
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[Step] = {
      Step.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Step] = {
    for {
      node <- Neo4jRepository.findNodeById(id)
      scriptDefNodeId <- Neo4jRepository.getChildNodeId(id, stepAndScriptDef)
      scriptDef <- ScriptDefinition.fromNeo4jGraph(scriptDefNodeId)
      stepInputNodeId <- Neo4jRepository.getChildNodeId(id, stepAndStepInput)
      stepInput <- StepInput.fromNeo4jGraph(stepInputNodeId)
      scopeNodeId <- Neo4jRepository.getChildNodeId(id, stepAndInventoryExec)
      scope <- InventoryExecutionScope.fromNeo4jGraph(scopeNodeId)
      reportNodeId <- Neo4jRepository.getChildNodeId(id, stepAndReport)
      report <- StepExecutionReport.fromNeo4jGraph(reportNodeId)
    } yield {
      val map = Neo4jRepository.getProperties(node, "stepId", "name", "description", "stepType", "executionOrder")
      val stepNodeIds = Neo4jRepository.getChildNodeIds(id, stepAndStep)
      val steps = stepNodeIds.flatMap(nodeId => Step.fromNeo4jGraph(nodeId))
      Step(Some(id),
        map("stepId").asInstanceOf[String],
        map("name").asInstanceOf[String],
        map("description").asInstanceOf[String],
        StepType.toStepType(map("stepType").asInstanceOf[String]),
        scriptDef,
        stepInput,
        scope,
        map("executionOrder").asInstanceOf[Int],
        steps,
        report
      )
    }
  }
}