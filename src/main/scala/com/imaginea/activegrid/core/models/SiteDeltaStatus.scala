package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 18/11/16.
  */
sealed trait SiteDeltaStatus {
  def deltaStatus: String

  override def toString: String = super.toString
}

case object CHANGED extends SiteDeltaStatus {
  override def deltaStatus: String = "CHANGED"
}

case object UNCHANGED extends SiteDeltaStatus {
  override def deltaStatus: String = "UNCHANGED"
}

case object SiteDeltaStatus {
  def toDeltaStatus(deltaStatus: String): SiteDeltaStatus = {
    deltaStatus match {
      case "CHANGED" => CHANGED
      case "UNCHANGED" => UNCHANGED
    }
  }
}
