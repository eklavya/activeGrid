package com.imaginea.activegrid.core.models

/**
  * Created by shareefn on 8/12/16.
  */
sealed trait LineType {
  def lineType: String

  override def toString: String = super.toString
}

case object DIRECTORY extends LineType {
  override def lineType: String = "DIRECTORY"
}

case object FILE extends LineType {
  override def lineType: String = "FILE"
}

case object RESULT extends LineType {
  override def lineType: String = "RESULT"
}

case object LineType {
  def toLineType(lineType: String): LineType = {
    lineType match {
      case "DIRECTORY" => DIRECTORY
      case "FILE" => FILE
      case "RESULT" => RESULT
    }
  }
}