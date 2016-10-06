package com.imaginea.activegrid.core.models

/**
  * Created by babjik on 26/9/16.
  */
case class ResourceACL (resources: String  = ResourceType.All.toString
                        ,permission: String = Permission.All.toString
                        ,val resourceIds: Array[Long] = Array.empty) extends BaseEntity

object ResourceACLProtocol{
  val label = "ResourceACL"
}