package com.imaginea.activegrid.core.models

import org.neo4j.graphdb.Node

/**
  * Created by sivag on 18/1/17.
  */
//RWF - Running Work Flows.
class RunningWorkflow(override val id: Option[Long], workflowId: Long) extends BaseEntity

object CurrentRunningWorkflows {

  val lable = "CurrnetWorkflows"
  val runningWorkLowLable = "Running"

  def add(workflowId: Long): Unit = {
    val currentWorkflowNode = Neo4jRepository.getOrSaveEntity(workflowId.toString, None)
    val parentNode = Neo4jRepository.getNodesByLabel(lable).headOption.getOrElse(Neo4jRepository.getOrSaveEntity(lable, None))
    Neo4jRepository.createRelation(runningWorkLowLable, parentNode, currentWorkflowNode)
  }

  def remove(workflowId: Long): Unit = {
    Neo4jRepository.getNodesByLabel(lable).headOption.foreach {
      fromnode =>
        val mayBeToNode = Neo4jRepository.getNodesWithRelation(fromnode, runningWorkLowLable).headOption
        mayBeToNode.foreach {
          toNode =>
            Neo4jRepository.deleteRelationship(fromnode.getId, toNode.getId, runningWorkLowLable)
            toNode.delete()
        }
    }
  }

  def size(): Int = {
    Neo4jRepository.getNodesByLabel(lable).headOption.map {
      root => Neo4jRepository.getNodesWithRelation(root, runningWorkLowLable).size
    }.getOrElse(0)
  }

  def get(workflowId: Long): Option[Node] = {
    Neo4jRepository.getNodesByLabel(workflowId.toString).headOption
  }

}
