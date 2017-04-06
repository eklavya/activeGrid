package com.imaginea.activegrid.core.models

import com.imaginea.Main
import com.imaginea.activegrid.core.utils.ActiveGridUtils
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by nagulmeeras on 13/01/17.
  */
class AnsibleWorkflowProcessor {
  val workflowExecutors = mutable.HashMap.empty[Long, Future[Unit]]

  def stopWorkflow(workflow: Workflow): Boolean = {
    workflow.id.exists(id => workflowExecutors.remove(id).isDefined)
  }
}

object AnsibleWorkflowProcessor extends WorkflowProcessor {
  val logger = Logger(LoggerFactory.getLogger(AnsibleWorkflowProcessor.getClass.getName))

  override def executeWorkflow(workflowContext: WorkflowContext, async: Boolean): Unit = {
    //scalastyle:off magic.number
    implicit val system = Main.system
    implicit val materializer = Main.materializer
    implicit val executionContext = ActiveGridUtils.getExecutionContextByService("workflow")
    val workflow = workflowContext.workflow
    val playBookRunner = new AnsiblePlayBookRunner(workflowContext)
    if (CurrentRunningWorkflows.size > WorkflowConstants.maxParlellWorkflows) {
      logger.error(s"Too many parlell workflows triggered, Maximum allowed parllel worksflows are ${WorkflowConstants.maxParlellWorkflows}")
    } else {
      val workflowId = workflow.id.getOrElse(0L)
      if (CurrentRunningWorkflows.get(workflowId).isEmpty) {
        if (async) {
          system.scheduler.schedule(2.seconds, 2.seconds) {
            playBookRunner.execute()
          }
        } else {
          playBookRunner.execute()
        }
      } else {
        logger.info(s"Workflow ${workflow.name} is currently running, Please try after some time.")
      }
    }
  }

  def stopWorkflow(workflow: Workflow): Unit = {
    workflow.id.foreach {
      id => CurrentRunningWorkflows.remove(id)
    }
  }
}
