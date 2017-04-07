package com.imaginea.actors

import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import akka.pattern._
import akka.util.Timeout
import com.imaginea.Main
import com.imaginea.Main.system.dispatcher
import com.imaginea.activegrid.core.models.{WORKFLOW_IDLE, WORKFLOW_NOTFOUND, WORKFLOW_RUNNING, WorkflowStatus}
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

/**
  * Created by sivag on 3/3/17.
  */
object WorkflowDDHandler {

  val replicator = DistributedData(Main.system).replicator
  implicit val node = Cluster(Main.system)
  val logger = LoggerFactory.getLogger(getClass)
  val mapKey = LWWMapKey[WorkflowStatus]("WorkflowUpdate")
  val readMajority = ReadMajority(5.seconds)
  val writeMajority = WriteMajority(5.seconds)
  implicit val timeout: Timeout = 5.seconds

  //scalastyle:off method.length
  def getWorkflowStatus(workflowId: String): Option[WorkflowStatus] = {
    logger.info(s"Checking status of $workflowId")
    val readMajority = ReadMajority(5.seconds)
    val maybeStatus = replicator ? Get(mapKey, readMajority)
    val status = maybeStatus map {
      case g@GetSuccess(mapKey, req) =>
        val runningWorkflows = g.get(mapKey).asInstanceOf[LWWMap[WorkflowStatus]]
        runningWorkflows.get(workflowId).getOrElse(WORKFLOW_NOTFOUND)
      case g@GetFailure(mapKey, req) =>
        logger.info(s"Error while fetching current running workflow, Workflow($workflowId)")
        WORKFLOW_NOTFOUND
    }
    val result = Try {
      Await.result(status, 1.seconds)
    }
    result.toOption
  }

  def removeWorkflow(workflowId: String): Unit = {
    getWorkflowStatus(workflowId).map { status =>
      Update(mapKey, LWWMap.empty[WorkflowStatus], writeMajority)(_.remove(node, workflowId))
    }
  }

  def getRunningWorkflows(): LWWMap[WorkflowStatus] = {
    logger.info("Fetching all running workflows")
    val maybeList = Try {
      val result = replicator ? Get(mapKey, readMajority) map {
        case list@GetSuccess(mapKey, req) => list.get(mapKey).asInstanceOf[LWWMap[WorkflowStatus]]
      }
      Await.result(result, 2.seconds)
    }
    maybeList.toOption.getOrElse(LWWMap.empty[WorkflowStatus])
  }

  def startWorkflows(workflowId: String): Unit = {
    getWorkflowStatus(workflowId).map {
      case WORKFLOW_NOTFOUND | WORKFLOW_IDLE =>
        Update(mapKey, LWWMap.empty[WorkflowStatus], writeMajority)(_.put(node, workflowId, WORKFLOW_RUNNING))
      case WORKFLOW_RUNNING => logger.info(s"$workflowId is already running")
      case _ => logger.error("Unknow worklfow status")
    }
  }
}


