package com.imaginea.activegrid.core.models

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 17/1/17.
  */

object AnsibleWorkflowProcessor extends WorkflowProcessor {

  override def stopWorkflow(workflow: Workflow): Unit = ???

  val workflowExecutors = Map.empty[Object, ScheduledExecutorService]
  val logger = Logger(LoggerFactory.getLogger(AnsibleWorkflowProcessor.getClass.getName))

  override def executeWorkflow(workflowContext: WorkflowContext, async: Boolean): Unit = {

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
      val schedular = Executors.newScheduledThreadPool(1);
      schedular.schedule(playBookRunner, 2000l, TimeUnit.MILLISECONDS)
    } else {
      playBookRunner.run()
    }
  }
}
