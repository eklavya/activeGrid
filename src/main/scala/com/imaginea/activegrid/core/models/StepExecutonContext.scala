package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 04/01/17.
  */
class StepExecutonContext(step: Step,
                          cumulativeStepExecutionStatus: CumulativeStepExecutionStatus,
                          stepExecutionListeners: List[StepExecutonContext]
                         )
