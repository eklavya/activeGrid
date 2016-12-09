package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 09/12/16.
  */
sealed trait LineType {
  val lineType: String
}

case object DIRECTORY extends LineType {
  override val lineType: String = "DIRECTORY"
}

case object FILE extends LineType {
  override val lineType: String = "FILE"
}

case object RESULT extends LineType {
  override val lineType: String = "RESULT"
}

case object LineType {
  def toLineType(lineTypeName: String): LineType = {
    lineTypeName match {
      case "DIRECTORY" => DIRECTORY
      case "FILE" => FILE
      case "RESULT" => RESULT
    }
  }
}