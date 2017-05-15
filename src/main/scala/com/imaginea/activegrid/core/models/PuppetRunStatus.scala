package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 11/5/17.
  */
sealed trait PuppetRunStatus {
  val puppetRunStatus: String

  override def toString: String = puppetRunStatus
}

object PuppetRunStatus {

  case object FAILED extends PuppetRunStatus {
    override val puppetRunStatus: String = "failed"
  }

  case object CHANGED extends PuppetRunStatus {
    override val puppetRunStatus: String = "changed"
  }

  case object UNCHANGED extends PuppetRunStatus {
    override val puppetRunStatus: String = "unchanged"
  }

  def toPuppetRunStatus(puppetRunStatus: String): PuppetRunStatus = {
    puppetRunStatus match {
      case "failed" => FAILED
      case "skipped" => CHANGED
      case "completed" => UNCHANGED
    }
  }
}
