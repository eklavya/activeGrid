package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class NodeReport(override val id: Option[Long],
                      node: Instance,
                      response: String,
                      taskReports: List[TaskReport],
                      status: StepExecutionStatus) extends BaseEntity
