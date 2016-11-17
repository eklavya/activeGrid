package com.imaginea.activegrid.core.models

/**
  * Created by nagulmeeras on 01/11/16.
  */
sealed trait GroupType {
  def groupType: String
}

case object GroupType {

  case object Role extends GroupType {
    override def groupType: String = "role"
  }

  def toGroupType(groupType: String): GroupType = {
    groupType match {
      case "role" => Role
    }
  }
}
