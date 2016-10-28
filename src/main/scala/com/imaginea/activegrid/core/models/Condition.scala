package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 25/10/16.
  */
sealed trait Condition {
  def condition: String

  override def toString: String = super.toString
}

case object EQUALS extends Condition {
  override def condition: String = "EQUALS"
}

case object CONTAINS extends Condition {
  override def condition: String = "CONTAINS"
}

case object STARTSWITH extends Condition {
  override def condition: String = "STARTSWITH"
}

case object ENDSWITH extends Condition {
  override def condition: String = "ENDSWITH"
}

object Condition {
  def toCondition(name: String): Condition = name match {
    case "EQUALS" => EQUALS
    case "CONTAINS" => CONTAINS
    case "STARTSWITH" => STARTSWITH
    case "ENDSWITH" => ENDSWITH
  }
}
