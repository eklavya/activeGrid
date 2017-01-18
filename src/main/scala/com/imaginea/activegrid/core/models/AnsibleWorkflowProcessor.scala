package com.imaginea.activegrid.core.models

import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import akka.actor.Props
import com.imaginea.Main
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration

/**
  * Created by sivag on 17/1/17.
  */

object AnsibleWorkflowProcessor extends WorkflowProcessor {

  def stopWorkflow(workflow: Workflow): Unit = {
    //todo implementation
  }

  val workflowExecutors = Map.empty[Long, ScheduledExecutorService]
  val logger = Logger(LoggerFactory.getLogger(AnsibleWorkflowProcessor.getClass.getName))

  override def executeWorkflow(workflowContext: WorkflowContext, async: Boolean): Unit = {
    //scalastyle:off magic.number
    implicit val system = Main.system
    implicit val materializer = Main.materializer
    implicit val executionContext = system.dispatcher
    val workflow = workflowContext.getWorkflow()
    val playBookRunner = new AnsiblePlayBookRunner(workflowContext)
    if (async) {
      if (workflowExecutors.size > WorkflowConstants.maxParlellWorkflows) {
        logger.error("Too many parlell workflows trigger, Max parllel works ares " + WorkflowConstants.maxParlellWorkflows)
      }
      val workflowId = workflow.id.getOrElse(0L)
      if (workflowExecutors.contains(workflowId)) {
        logger.info("Workflow [" + workflow.name + "] is currently running, Please try after some time.")
      }
      system.scheduler.scheduleOnce(Duration.create(50L, TimeUnit.MILLISECONDS), playBookRunner)
    }
    else {
      playBookRunner.run()
    }
  }
}

