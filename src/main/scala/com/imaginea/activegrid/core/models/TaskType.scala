package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
sealed trait TaskType {
  val taskType: String

  override def toString: String = super.toString
}

case object TASK extends TaskType {
  override val taskType: String = "TASK"
}

case object HANDLER_TASK extends TaskType {
  override val taskType: String = "HANDLER_TASK"
}

object TaskType {
  def toTaskType(taskType: String): TaskType = {
    taskType match {
      case "TASK" => TASK
      case "HANDLER_TASK" => HANDLER_TASK
    }
  }
}
