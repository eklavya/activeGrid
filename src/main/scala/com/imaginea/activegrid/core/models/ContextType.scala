package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 24/11/16.
  */
sealed trait ContextType {
  def contextType: String

  override def toString: String = super.toString
}

case object USER_HOME extends ContextType {
  override def contextType: String = "USER_HOME"
}

case object SITE extends ContextType {
  override def contextType: String = "SITE"
}

case object INSTANCE extends ContextType {
  override def contextType: String = "INSTANCE"
}

case object ContextType {
  def toContextType(contextType: String): ContextType = {
    contextType match {
      case "USER_HOME" => INSTANCE
      case "SITE" => SITE
      case "INSTANCE" => INSTANCE
    }
  }
}
