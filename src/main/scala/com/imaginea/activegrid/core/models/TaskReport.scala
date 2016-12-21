package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class TaskReport(override val id: Option[Long],
                      task: Task,
                      status: TaskStatus,
                      result: String) extends BaseEntity
