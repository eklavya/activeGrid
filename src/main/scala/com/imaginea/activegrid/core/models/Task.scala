package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
case class Task(override val id: Option[Long],
                name: String,
                content: String,
                taskType: TaskType,
                path: String) extends BaseEntity