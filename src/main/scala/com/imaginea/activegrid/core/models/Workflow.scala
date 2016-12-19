package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 19/12/16.
  */
case class Workflow(override val id: Option[Long],
                     name: String,
                    description: String,
                    mode: WorkflowMode,
                    steps: List[Step],
                    stepOrderStrategy: StepOrderStrategy,
                    executionStrategy: WorkflowExecutionStrategy,
                    execution: WorkflowExecution,
                    executionHistory: List[WorkflowExecution],
                    lastExecutionBy: String,
                    lastExecutionAt: Long) extends BaseEntity
