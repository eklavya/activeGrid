package com.imaginea.activegrid.core.models

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

/**
  * Created by sivag on 17/1/17.
  */
object WorkflowServiceFactory {


  val logger = Logger(LoggerFactory.getLogger(WorkflowServiceFactory.this.getClass.getName))
  /**
    *
    * @param mode
    * @return WorkflowProcess base given mode.
    */
  def getWorkflowModeProcessor(mode: WorkflowMode): Option[WorkflowProcessor] = {
    logger.info("Fetching workflow mode processor for [" + mode + "]")
    mode match {
      case AGENT => Some(PuppetWorkflowProcessor)
      case AGENT_LESS => Some(AnsibleWorkflowProcessor.getProcessor)
      case _ => None
    }
  }
}

