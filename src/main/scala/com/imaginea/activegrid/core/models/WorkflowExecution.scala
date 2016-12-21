package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by nagulmeeras on 20/12/16.
  */
case class WorkflowExecution(override val id: Option[Long],
                             executionTime: Long,
                             executionBy: String,
                             status: WorkFlowExecutionStatus,
                             currentStep: Option[Step],
                             logs: List[String],
                             inventory: Option[Inventory],
                             stepExecutionReports: List[StepExecutionReport]) extends BaseEntity

object WorkflowExecution {
  val labelName = "WorkflowExecution"
  val workflowAndInventoryRelation = "HAS_Inventory"

  //TODO Need to write models for Step and StepExecutionReport then we have to integrate here
  implicit class WorkflowExecutionImpl(workflowExecution: WorkflowExecution) extends Neo4jRep[WorkflowExecution] {
    override def toNeo4jGraph(entity: WorkflowExecution): Node = {
      val map = Map("executionTime" -> entity.executionTime,
        "executionBy" -> entity.executionBy,
        "status" -> entity.status.executionStatus,
        "logs" -> entity.logs.toArray)
      val parentNode = Neo4jRepository.saveEntity(labelName, entity.id, map)
      entity.inventory.foreach {
        inventory =>
          val inventoryNode = inventory.toNeo4jGraph(inventory)
          Neo4jRepository.createRelation(workflowAndInventoryRelation, parentNode, inventoryNode)
      }

      parentNode
    }

    override def fromNeo4jGraph(id: Long): Option[WorkflowExecution] = {
      WorkflowExecution.fromNeo4jGraph(id)
    }
  }

  def fromNeo4jGraph(id: Long): Option[WorkflowExecution] = {
    val mayBeNode = Neo4jRepository.findNodeById(id)
    mayBeNode.map {
      node =>
        val map = Neo4jRepository.getProperties(node, "executionTime", "executionBy", "status", "logs")
        val inventoryNodeId = Neo4jRepository.getChildNodeId(id, workflowAndInventoryRelation)
        val inventory = inventoryNodeId.flatMap(nodeId => Inventory.fromNeo4jGraph(nodeId))
        WorkflowExecution(Some(id),
          map("executionTime").asInstanceOf[Long],
          map("executionBy").asInstanceOf[String],
          WorkFlowExecutionStatus.toExecutionStatus(map("status").asInstanceOf[String]),
          None,
          map("logs").asInstanceOf[Array[String]].toList,
          inventory,
          List.empty[StepExecutionReport]
        )
    }
  }
}