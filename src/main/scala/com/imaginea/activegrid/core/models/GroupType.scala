package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 3/11/16.
  */
trait GroupType {
  def  groupType:String
}
object Role extends GroupType{
  override def groupType: String = "role"
}
