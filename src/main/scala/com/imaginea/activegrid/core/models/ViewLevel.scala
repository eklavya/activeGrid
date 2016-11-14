package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 2/11/16.
  */
sealed trait ViewLevel {
  def viewLevel: String

  override def toString: String = super.toString
}

case object SUMMARY extends ViewLevel {
  override def viewLevel: String = "SUMMARY"
}

case object DETAILED extends ViewLevel {
  override def viewLevel: String = "DETAILED"
}

case object ViewLevel {
  def toViewLevel(viewLevel: String): ViewLevel = {
    viewLevel match {
      case "SUMMARY" => SUMMARY
      case "DETAILED" => DETAILED
      case _ => SUMMARY
    }
  }
}


