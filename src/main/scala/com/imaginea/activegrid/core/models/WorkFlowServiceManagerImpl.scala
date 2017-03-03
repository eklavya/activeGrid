package com.imaginea.activegrid.core.models

import akka.actor.Props
import com.imaginea.Main
import com.imaginea.actors.{WofklowActor, WrkFlow}

import scala.collection.immutable.HashMap
import akka.pattern.ask

import scala.concurrent.Await
import scala.concurrent.duration._
import akka.util.Timeout


/**
  * Created by sivag on 10/1/17.
  */
class WorkFlowServiceManagerImpl {

  //Todo 1. WorkFlowContext bean and  2. settingCurrentworkflows.
  def currentWorkFlows = HashMap.empty[Int, WorkflowContext]

  def getWorkFlow(id: Long): Option[Workflow] = {
    Neo4jRepository.findNodeByLabelAndId(Workflow.labelName, id).flatMap {
      workFlowNode => Workflow.fromNeo4jGraph(workFlowNode.getId)
    }
  }
  def isWorkflowRunning(workflowId: Long): Boolean = {
    WrkFlow(workflowId.toString,"GETSTATUS")
    val actor = Main.system.actorOf(Props[WofklowActor])
    implicit val timeout: Timeout = 5.seconds
    Await.result(actor ? WrkFlow,5 seconds).asInstanceOf[Boolean]
  }

  def execute(workflow: Option[Workflow], async: Boolean): Unit = {
    workflow.map {
      wf =>
        val execution = wf.execution
        execution.map {
          exec =>
            wf.id.map { workflowId =>
              // TODO clearlog implementation.
              WorkflowExecution.clearLogs()
              //TODO Authentication Mechansim to extract current user
              val currentUser = "anonymous"
              import java.util.Calendar
              val currentTime = Calendar.getInstance().getTime
              val executionUpdate = Map("executionTime" -> currentTime, "executionBy" -> currentUser, "status" -> "STARTED")
              Neo4jRepository.updateNodeByLabelAndId[WorkflowExecution](WorkflowExecution.labelName,
                exec.id.getOrElse(0L), executionUpdate)
              val logListener = WorkflowExecLogListener.get()
              val workFlowUpdate = Map("executionTime" -> currentTime, "executionBy" -> currentUser)
              val workflowListener: WorkflowListener = new WorkflowExecutionListener()
              val workflowExecLogListener = WorkflowExecLogListener.get()
              val workflowContext: WorkflowContext = new WorkflowContext(wf, workflowListener, workflowExecLogListener,None,None,None)
              WorkflowServiceFactory.getWorkflowModeProcessor(wf.mode.getOrElse(WorkflowMode.toWorkFlowMode("AGENT"))).map {
                processor => processor.executeWorkflow(workflowContext, async)
              }
              Neo4jRepository.updateNodeByLabelAndId[Workflow](Workflow.labelName, workflowId, workFlowUpdate)
              CurrentRunningWorkflows.add(workflowId)
            }
        }


    }
  }

}
