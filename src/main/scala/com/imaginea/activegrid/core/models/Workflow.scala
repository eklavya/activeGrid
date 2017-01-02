package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 20/12/16.
  */
case class Workflow(override val id: Option[Long],
                    name: String,
                    description: String,
                    mode: WorkflowMode,
                    steps: List[Step],
                    stepOrderStrategy: StepOrderStrategy,
                    executionStrategy: WorkflowExecutionStrategy,
                    execution: Option[WorkflowExecution],
                    executionHistory: List[WorkflowExecution],
                    lastExecutionBy: String,
                    lastExecutionAt: Long) extends BaseEntity

object Workflow {
  val labelName = "Workflow"
  val workflowAndExecutionRelation = "HAS_WorkflowExecution"
  val workflowAndExecHistoryRelation = "HAS_ExecutionHistory"

  //TODO Need integrate Step model in this
  implicit class WorkflowImpl(workFlow: Workflow) extends Neo4jRep[Workflow] {

    override def toNeo4jGraph(entity: Workflow): Node = {
      val map = Map("name" -> entity.name,
        "description" -> entity.description,
        "mode" -> entity.mode.toString,
        "stepOrderStrategy" -> entity.stepOrderStrategy.orderStrategy,
        "executionStrategy" -> entity.executionStrategy.executionStrategy,
        "lastExecutionBy" -> entity.lastExecutionBy,
        "lastExecutionAt" -> entity.lastExecutionAt
      )
      val parentNode = Neo4jRepository.saveEntity[Workflow](labelName, entity.id, map)
      entity.execution.foreach { execution =>
        val workflowExecutionNode = execution.toNeo4jGraph(execution)
        Neo4jRepository.createRelation(workflowAndExecutionRelation, parentNode, workflowExecutionNode)
      }
      entity.executionHistory.foreach { exection =>
        val childNode = exection.toNeo4jGraph(exection)
        Neo4jRepository.createRelation(workflowAndExecHistoryRelation, parentNode, childNode)
      }
      parentNode

    }

    override def fromNeo4jGraph(id: Long): Option[Workflow] = {
      Workflow.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[Workflow] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map { node =>
      val map = Neo4jRepository.getProperties(node, "name", "description", "mode", "stepOrderStrategy",
        "executionStrategy", "lastExecutionBy", "lastExecutionAt")
      val executionNodeid = Neo4jRepository.getChildNodeId(id, workflowAndExecutionRelation)
      val execution = executionNodeid.flatMap(nodeId => WorkflowExecution.fromNeo4jGraph(nodeId))
      val executionHistoryIds = Neo4jRepository.getChildNodeIds(id, workflowAndExecHistoryRelation)
      val executionHistory = executionHistoryIds.flatMap(nodeId => WorkflowExecution.fromNeo4jGraph(nodeId))
      Workflow(Some(id),
        map("name").asInstanceOf[String],
        map("description").asInstanceOf[String],
        WorkflowMode.toWorkFlowMode(map("mode").asInstanceOf[String]),
        List.empty[Step],
        StepOrderStrategy.toOrderStrategy(map("stepOrderStrategy").asInstanceOf[String]),
        WorkflowExecutionStrategy.toExecutionStrategy(map("executionStrategy").asInstanceOf[String]),
        execution,
        executionHistory,
        map("lastExecutionBy").asInstanceOf[String],
        map("lastExecutionAt").asInstanceOf[Long]
      )
    }
  }
}
