package com.imaginea.activegrid.core.models

/**
  * Created by sivag on 3/11/16.
  */
case class InstanceGroup(override val id:Option[Long],groupType:GroupType,name:String,instances:List[Instance]) extends BaseEntity{

}
