package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 18/1/17.
  */
class CurrentRunningWorkflows extends BaseEntity {
  val lable = "RunningWorkflows"
  override val id: Option[Long] = _

  def addWorkflow(id:Long) : Boolean = {
    Neo4jRepository.saveEntity[CurrentRunningWorkflows](lable,Some(id),)
  }

}
