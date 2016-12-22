package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 20/12/16.
  */
sealed trait VariableScope {
  val variableScope: String

  override def toString: String = super.toString
}

case object GROUP extends VariableScope {
  override val variableScope: String = "GROUP"
}

case object HOST extends VariableScope {
  override val variableScope: String = "HOST"
}

case object UNKNOWN extends VariableScope {
  override val variableScope: String = "UNKNOWN"
}

case object EXTRA extends VariableScope {
  override val variableScope: String = "EXTRA"
}

object VariableScope {
  def toVariableScope(variableScope: String): VariableScope = {
    variableScope match {
      case "GROUP" => GROUP
      case "HOST" => HOST
      case "UNKNOWN" => UNKNOWN
      case "EXTRA" => EXTRA
    }
  }
}
