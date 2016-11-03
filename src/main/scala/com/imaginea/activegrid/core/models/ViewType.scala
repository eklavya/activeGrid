package com.imaginea.activegrid.core.models

/**
  * Created by sampathr on 3/11/16.
  */
sealed trait ViewType {
  def viewType: String

  override def toString: String = super.toString
}

case object LIST extends ViewType {
  override def viewType: String = "LIST"
}

case object OPERATIONS extends ViewType {
  override def viewType: String = "OPERATIONS"
}

case object ARCHITECTURE extends ViewType {
  override def viewType: String = "ARCHITECTURE"
}

case object ViewType {
  def toViewType(viewType: String): ViewType = {
    viewType match {
      case "LIST" => LIST
      case "OPERATIONS" => OPERATIONS
      case "ARCHITECTURE" => ARCHITECTURE
    }
  }
}
