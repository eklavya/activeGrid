
package com.imaginea.activegrid.core.models

import com.imaginea.activegrid.core.models.StepOrderStrategy.SEQUENTIAL
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
;

class WorkflowStrategyFactory {

	val logger = Logger(LoggerFactory.getLogger(WorkflowStrategyFactory.this.getClass.getName))

  //todo stepExecutionProcessor implementation
	def   getWorkflowProcessor(stepOrderStrategy: Option[StepOrderStrategy]) : Option[SequentialWorkflowProcessor]  =
  {
		stepOrderStrategy.map {
      stratagy => stratagy  match  {
			case SEQUENTIAL =>  new SequentialWorkflowProcessor();
      //todo other cases
			}
		}
	}
}
