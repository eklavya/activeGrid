package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 20/12/16.
  */
case class Workflow(override val id: Option[Long],
                    name: String,
                    description: Option[String],
                    mode: Option[WorkflowMode],
                    steps: List[Step],
                    stepOrderStrategy: Option[StepOrderStrategy],
                    executionStrategy: Option[WorkflowExecutionStrategy],
                    execution: Option[WorkflowExecution],
                    executionHistory: List[WorkflowExecution],
                    lastExecutionBy: Option[String],
                    lastExecutionAt: Long,
                    module: Option[Module],
                    playBooks: List[AnsiblePlayBook],
                    publishedInventory: Option[Inventory]) extends BaseEntity

object Workflow {
  val labelName = "Workflow"
  val workflowAndExecutionRelation = "HAS_WorkflowExecution"
  val workflowAndExecHistoryRelation = "HAS_ExecutionHistory"
  val workflowAndModuleRelation = "HAS_Module"
  val workflowAndPlayBookRelation = "HAS_PlayBook"
  val workflowAndInventoryRelation = "HAS_Inventory"

  def apply(name: String, module: Module, playBooks: List[AnsiblePlayBook]): Workflow =
    Workflow(None, name, None, None, List.empty[Step], None, None, None, List.empty[WorkflowExecution],
      None, 0L, Some(module), List.empty[AnsiblePlayBook], None)

  //TODO Need integrate Step model in this
  implicit class WorkflowImpl(workFlow: Workflow) extends Neo4jRep[Workflow] {

    override def toNeo4jGraph(entity: Workflow): Node = {
      val map = Map("name" -> entity.name,
        "description" -> entity.description,
        "mode" -> entity.mode.map(_.workflowMode),
        "stepOrderStrategy" -> entity.stepOrderStrategy.map(_.orderStrategy),
        "executionStrategy" -> entity.executionStrategy.map(_.executionStrategy),
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
      entity.module.foreach { module =>
        val childNode = module.toNeo4jGraph(module)
        Neo4jRepository.createRelation(workflowAndModuleRelation, parentNode, childNode)
      }
      entity.playBooks.foreach { playBook =>
        val playBookNode = playBook.toNeo4jGraph(playBook)
        Neo4jRepository.createRelation(workflowAndPlayBookRelation, parentNode, playBookNode)
      }
      entity.publishedInventory.foreach { inventory =>
        val childNode = inventory.toNeo4jGraph(inventory)
        Neo4jRepository.createRelation(workflowAndInventoryRelation, parentNode, childNode)
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
      val moduleNodeId = Neo4jRepository.getChildNodeId(id, workflowAndModuleRelation)
      val module = moduleNodeId.flatMap(nodeId => Module.fromNeo4jGraph(nodeId))
      val playBookIds = Neo4jRepository.getChildNodeIds(id, workflowAndPlayBookRelation)
      val playBooks = playBookIds.flatMap(nodeId => AnsiblePlayBook.fromNeo4jGraph(nodeId))
      val inventoryNodeId = Neo4jRepository.getChildNodeId(id, workflowAndInventoryRelation)
      val publishedInventory = inventoryNodeId.flatMap(nodeId => Inventory.fromNeo4jGraph(nodeId))
      Workflow(Some(id),
        map("name").asInstanceOf[String],
        map.get("description").asInstanceOf[Option[String]],
        map.get("mode").asInstanceOf[Option[String]].map(WorkflowMode.toWorkFlowMode),
        List.empty[Step],
        map.get("stepOrderStrategy").asInstanceOf[Option[String]].map(StepOrderStrategy.toOrderStrategy),
        map.get("executionStrategy").asInstanceOf[Option[String]].map(WorkflowExecutionStrategy.toExecutionStrategy),
        execution,
        executionHistory,
        map.get("lastExecutionBy").asInstanceOf[Option[String]],
        map("lastExecutionAt").asInstanceOf[Long],
        module,
        playBooks,
        publishedInventory
      )
    }
  }
}
