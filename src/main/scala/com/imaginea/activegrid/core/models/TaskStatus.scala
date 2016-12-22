package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
sealed trait TaskStatus {
  val taskStatus: String

  override def toString: String = super.toString
}

object TaskStatus {

  case object FAILED extends TaskStatus {
    override val taskStatus: String = "failed"
  }

  case object SKIPPED extends TaskStatus {
    override val taskStatus: String = "skipped"
  }

  case object COMPLETED extends TaskStatus {
    override val taskStatus: String = "completed"
  }

  case object PENDING extends TaskStatus {
    override val taskStatus: String = "pending"
  }

  case object UNKNOWN extends TaskStatus {
    override val taskStatus: String = "unknown"
  }

  def toTaskStatus(taskStatus: String): TaskStatus = {
    taskStatus match {
      case "failed" => FAILED
      case "skipped" => SKIPPED
      case "completed" => COMPLETED
      case "pending" => PENDING
      case "unknown" => UNKNOWN
    }
  }
}

