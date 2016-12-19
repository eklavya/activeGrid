package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 19/12/16.
  */
case class Step(override val id: Option[Long],
                 stepId: String,
                name: String,
                description: String,
                stepType: StepType,
                scriptDefinition: ScriptDefinition,
                input: StepInput,
                scope: ExecutionScope,
                executionOrder: Int,
                childStep: List[Step],
                report: StepExecutionReport) extends BaseEntity
//report might fail for puppet case