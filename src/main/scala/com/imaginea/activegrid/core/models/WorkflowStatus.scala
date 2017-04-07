package com.imaginea.activegrid.core.models
/**
  * Created by sivag on 7/4/17.
  */
sealed trait WorkflowStatus
case object WORKFLOW_IDLE extends WorkflowStatus
case object WORKFLOW_RUNNING extends WorkflowStatus
case object WORKFLOW_NOTFOUND extends WorkflowStatus