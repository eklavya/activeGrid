package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 02/11/16.
  */
sealed trait ViewType {
  def viewType: String
}

case object ViewType {

  case object LIST extends ViewType {
    override def viewType: String = "list"
  }

  case object OPERATIONS extends ViewType {
    override def viewType: String = "operations"
  }

  case object ARCHITECTURE extends ViewType {
    override def viewType: String = "architecture"
  }

  def toViewType(viewType: String): ViewType = {
    viewType match {
      case "list" => LIST
      case "operations" => OPERATIONS
      case "architecture" => ARCHITECTURE
    }
  }
}
