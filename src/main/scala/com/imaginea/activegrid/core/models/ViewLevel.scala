package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 25/10/16.
  */
sealed trait ViewLevel {
  def level : String
  override def toString: String = level
}

case object SUMMARY extends ViewLevel{
  override def level: String = "SUMMARY"
}

case object DETAILED extends ViewLevel{
  override def level: String = "DETAILED"
}

case object ViewLevelProvider {
  def toInstanceProvider(viewLevel: String) : ViewLevel = {
    viewLevel match {
      case "SUMMARY" => SUMMARY
      case "DETAILED" => DETAILED

    }
  }
}