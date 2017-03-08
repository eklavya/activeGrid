package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 29/11/16.
  */
sealed trait JobType {
  def jobType: String

  override def toString: String = jobType
}

case object WorkFlowJob extends JobType {
  override def jobType: String = "WORKFLOW"
}

case object ASPolicy extends JobType {
  override def jobType: String = "POLICY"
}

case object JobType {
  def convert(jtype: String): JobType = {
    jtype match {
      case "WORKFLOW" => WorkFlowJob
      case "POILCY" => ASPolicy
      case _ => ASPolicy
    }
  }
}
