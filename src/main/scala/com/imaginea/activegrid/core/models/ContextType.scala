package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 07/12/16.
  */
sealed trait ContextType {
  val contextType: String
}

case object USER_HOME extends ContextType {
  override val contextType: String = "USER_HOME"
}

case object SITE extends ContextType {
  override val contextType: String = "SITE"
}

case object INSTANCE extends ContextType {
  override val contextType: String = "INSTANCE"
}

case object ContextType {
  def toContextType(contextName: String): ContextType = {
    contextName match {
      case "USER_HOME" => USER_HOME
      case "SITE" => SITE
      case "INSTANCE" => INSTANCE
    }
  }
}
