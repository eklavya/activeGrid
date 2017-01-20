package com.imaginea.activegrid.core.models

import scala.collection.mutable
import scala.concurrent.Future

/**
  * Created by nagulmeeras on 13/01/17.
  */
class AnsibleWorkflowProcessor {
  val workflowExecutors = mutable.HashMap.empty[Long, Future[Unit]]

  def stopWorkflow(workflow: Workflow): Boolean = {
    workflow.id.exists(id => workflowExecutors.remove(id).isDefined)
  }
}
