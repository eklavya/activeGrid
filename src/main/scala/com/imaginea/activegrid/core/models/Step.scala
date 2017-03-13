package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.neo4j.graphdb.Node
import org.slf4j.LoggerFactory

/**
  * Created by shareefn on 19/12/16.
  */
case class Step(override val id: Option[Long],
                stepId: Option[String],
                name: String,
                description: Option[String],
                stepType: Option[StepType],
                script: Script,
                input: Option[StepInput],
                scope: Option[InventoryExecutionScope],
                executionOrder: Int,
                childStep: List[Step],
                report: Option[StepExecutionReport]) extends BaseEntity

object Step {
  val labelName = "Step"
  val stepAndScriptDef = "HAS_ScriptDefinition"
  val stepAndAnsiblePlay = "HAS_AnsiblePlay"
  val stepAndStepInput = "HAS_StepInput"
  val stepAndInventoryExec = "HAS_InvExecScope"
  val stepAndStep = "HAS_Step"
  val stepAndReport = "HAS_Report"
  val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  def apply(name: String, script: Script): Step =
    Step(None, None, name, None, None, script, None, None, 0, List.empty[Step], None)

  implicit class StepImpl(step: Step) extends Neo4jRep[Step] {
    override def toNeo4jGraph(entity: Step): Node = {
      val map = Map("stepId" -> entity.stepId,
        "name" -> entity.name,
        "description" -> entity.description,
        "stepType" -> entity.stepType.map(_.stepType),
        "executionOrder" -> entity.executionOrder)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      entity.script match {
        case scriptDef: ScriptDefinition =>
          val scriptDefNode = scriptDef.toNeo4jGraph(scriptDef)
          Neo4jRepository.createRelation(stepAndScriptDef, parentNode, scriptDefNode)
        case ansiblePlay: AnsiblePlay =>
          val ansiblePlayNode = ansiblePlay.toNeo4jGraph(ansiblePlay)
          Neo4jRepository.createRelation(stepAndAnsiblePlay, parentNode, ansiblePlayNode)
      }
      entity.input match {
        case Some(input) =>
          val stepInputNode = input.toNeo4jGraph(input)
          Neo4jRepository.createRelation(stepAndStepInput, parentNode, stepInputNode)
        case None => logger.info("entity STep has no step input")
      }
      entity.scope match {
        case Some(scope) =>
          val invScopeNode = scope.toNeo4jGraph(scope)
          Neo4jRepository.createRelation(stepAndInventoryExec, parentNode, invScopeNode)
        case None => logger.info("entity Step has no scope")
      }
      entity.childStep.foreach { step =>
        val stepNode = step.toNeo4jGraph(step)
        Neo4jRepository.createRelation(stepAndStep, parentNode, stepNode)
      }
      entity.report match {
        case Some(report) =>
          val reportNode = report.toNeo4jGraph(report)
          Neo4jRepository.createRelation(stepAndReport, parentNode, reportNode)
        case None => logger.info("entity Step has no report")
      }
      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[Step] = {
      Step.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Step] = {
    val scriptDefNodeId = Neo4jRepository.getChildNodeId(id, stepAndScriptDef)
    val mayBeScript: Option[Script] = scriptDefNodeId match {
      case Some(nodeId) => ScriptDefinition.fromNeo4jGraph(nodeId)
      case None =>
        val ansiblePlayNodeId = Neo4jRepository.getChildNodeId(id, stepAndAnsiblePlay)
        ansiblePlayNodeId.flatMap { nodeId => AnsiblePlay.fromNeo4jGraph(nodeId) }
    }
    for {
      node <- Neo4jRepository.findNodeById(id)
      script <- mayBeScript
      stepInputNodeId <- Neo4jRepository.getChildNodeId(id, stepAndStepInput)
      scopeNodeId <- Neo4jRepository.getChildNodeId(id, stepAndInventoryExec)
      reportNodeId <- Neo4jRepository.getChildNodeId(id, stepAndReport)
    } yield {
      val stepInput = StepInput.fromNeo4jGraph(stepInputNodeId)
      val scope = InventoryExecutionScope.fromNeo4jGraph(scopeNodeId)
      val report = StepExecutionReport.fromNeo4jGraph(reportNodeId)
      val map = Neo4jRepository.getProperties(node, "stepId", "name", "description", "stepType", "executionOrder")
      val stepNodeIds = Neo4jRepository.getChildNodeIds(id, stepAndStep)
      val steps = stepNodeIds.flatMap(nodeId => Step.fromNeo4jGraph(nodeId))
      Step(Some(id),
        map.get("stepId").asInstanceOf[Option[String]],
        map("name").asInstanceOf[String],
        map.get("description").asInstanceOf[Option[String]],
        map.get("stepType").asInstanceOf[Option[String]].map(StepType.toStepType),
        script,
        stepInput,
        scope,
        map("executionOrder").asInstanceOf[Int],
        steps,
        report
      )
    }
  }
}