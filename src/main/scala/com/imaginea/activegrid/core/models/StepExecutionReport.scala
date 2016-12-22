package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class StepExecutionReport(override val id: Option[Long],
                               step: Step,
                               nodeReports: List[NodeReport],
                               status: CumulativeStepExecutionStatus) extends BaseEntity
